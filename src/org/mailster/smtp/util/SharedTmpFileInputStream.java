package org.mailster.smtp.util;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)SharedFileInputStream.java       1.11 07/05/04
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.mail.internet.SharedInputStream;

/**
 * A <code>SharedFileInputStream</code> is a
 * <code>BufferedInputStream</code> that buffers
 * data from the file and supports the <code>mark</code>
 * and <code>reset</code> methods.  It also supports the
 * <code>newStream</code> method that allows you to create
 * other streams that represent subsets of the file.
 * A <code>RandomAccessFile</code> object is used to
 * access the file data. <p>
 *
 * @author Bill Shannon
 * @since JavaMail 1.4
 *
 * This class has been modified to ensure that the temporary 
 * underlying file is deleted when last reference is gone or 
 * when JVM exits normally.
 *
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SharedTmpFileInputStream extends BufferedInputStream implements SharedInputStream {

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * The file containing the data.
     * Shared by all related SharedFileInputStreams.
     */
    protected RandomAccessFile in;

    /**
     * The normal size of the read buffer.
     */
    protected int bufsize;

    /**
     * The file offset that corresponds to the first byte in
     * the read buffer.
     */
    protected long bufpos;

    /**
     * The file offset of the start of data in this subset of the file.
     */
    protected long start = 0;

    /**
     * The amount of data in this subset of the file.
     */
    protected long datalen;

    /**
     * A shared class that keeps track of the references
     * to a particular file so it can be closed when the
     * last reference is gone.
     */
    static class SharedFile {
        private int cnt;
        private final RandomAccessFile in;
        private final File sharedFile;

        SharedFile(String file) throws IOException {
            this(new File(file));
        }

        SharedFile(File file) throws IOException {
            this.sharedFile = file;

            // Always mark file to be deleted on exit in case streams
            // are not closed properly.
            this.sharedFile.deleteOnExit();

            this.in = new RandomAccessFile(file, "r");
        }

        public RandomAccessFile open() {
            cnt++;
            return in;
        }

        public synchronized void close() throws IOException {
            if (cnt > 0 && --cnt <= 0) {
                in.close();
                if (!sharedFile.delete())
                    throw new IOException(
                            "Some other process must be locking the file or file is write protected");
            }
        }

        public synchronized void forceClose() throws IOException {
            if (cnt > 0) {
                // normal case, close exceptions propagated
                cnt = 0;
                in.close();
            } else {
                // should already be closed, ignore exception
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        protected void finalize() throws Throwable {
            super.finalize();
            in.close();
        }
    }

    private SharedFile sf;

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (in == null)
            throw new IOException("Stream closed");
    }

    /**
     * Creates a <code>SharedFileInputStream</code>
     * for the file.
     *
     * @param   file   the file
     */
    public SharedTmpFileInputStream(File file) throws IOException {
        this(file, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a <code>SharedFileInputStream</code>
     * for the named file
     *
     * @param   file   the file
     */
    public SharedTmpFileInputStream(String file) throws IOException {
        this(file, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a <code>SharedFileInputStream</code>
     * with the specified buffer size.
     *
     * @param   file    the file
     * @param   size   the buffer size.
     * @exception IllegalArgumentException if size &lt;= 0.
     */
    public SharedTmpFileInputStream(File file, int size) throws IOException {
        super(null); // XXX - will it NPE?
        if (size <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        init(new SharedFile(file), size);
    }

    /**
     * Creates a <code>SharedFileInputStream</code>
     * with the specified buffer size.
     *
     * @param   file    the file
     * @param   size   the buffer size.
     * @exception IllegalArgumentException if size &lt;= 0.
     */
    public SharedTmpFileInputStream(String file, int size) throws IOException {
        super(null); // XXX - will it NPE?
        if (size <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        init(new SharedFile(file), size);
    }

    private void init(SharedFile sf, int size) throws IOException {
        this.sf = sf;
        this.in = sf.open();
        this.start = 0;
        this.datalen = in.length(); // XXX - file can't grow
        this.bufsize = size;
        buf = new byte[size];
    }

    /**
     * Used internally by the <code>newStream</code> method.
     */
    private SharedTmpFileInputStream(SharedFile sf, long start, long len, int bufsize) {
        super(null);
        this.sf = sf;
        this.in = sf.open();
        this.start = start;
        this.bufpos = start;
        this.datalen = len;
        this.bufsize = bufsize;
        buf = new byte[bufsize];
    }

    /**
     * Fills the buffer with more data, taking into account
     * shuffling and other tricks for dealing with marks.
     * Assumes that it is being called by a synchronized method.
     * This method also assumes that all data has already been read in,
     * hence pos > count.
     */
    private void fill() throws IOException {
        if (markpos < 0) {
            pos = 0; /* no mark: throw away the buffer */
            bufpos += count;
        } else if (pos >= buf.length) /* no room left in buffer */
            if (markpos > 0) { /* can throw away early part of the buffer */
                int sz = pos - markpos;
                System.arraycopy(buf, markpos, buf, 0, sz);
                pos = sz;
                bufpos += markpos;
                markpos = 0;
            } else if (buf.length >= marklimit) {
                markpos = -1; /* buffer got too big, invalidate mark */
                pos = 0; /* drop buffer contents */
                bufpos += count;
            } else { /* grow buffer */
                int nsz = pos * 2;
                if (nsz > marklimit)
                    nsz = marklimit;
                byte[] nbuf = new byte[nsz];
                System.arraycopy(buf, 0, nbuf, 0, pos);
                buf = nbuf;
            }
        count = pos;
        in.seek(bufpos + pos);
        // limit to datalen
        int len = buf.length - pos;
        if (bufpos - start + pos + len > datalen)
            len = (int) (datalen - (bufpos - start + pos));
        int n = in.read(buf, pos, len);
        if (n > 0)
            count = n + pos;
    }

    /**
     * See the general contract of the <code>read</code>
     * method of <code>InputStream</code>.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception IOException  if an I/O error occurs.
     */
    public synchronized int read() throws IOException {
        ensureOpen();
        if (pos >= count) {
            fill();
            if (pos >= count)
                return -1;
        }
        return buf[pos++] & 0xff;
    }

    /**
     * Read characters into a portion of an array, reading from the underlying
     * stream at most once if necessary.
     */
    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = count - pos;
        if (avail <= 0) {
//            if (false) {
//                /* If the requested length is at least as large as the buffer, and
//                   if there is no mark/reset activity, do not bother to copy the
//                   bytes into the local buffer.  In this way buffered streams will
//                   cascade harmlessly. */
//                if (len >= buf.length && markpos < 0) {
//                    // XXX - seek, update bufpos - how?
//                    return in.read(b, off, len);
//                }
//            }
            fill();
            avail = count - pos;
            if (avail <= 0)
                return -1;
        }
        int cnt = Math.min(avail, len);
        System.arraycopy(buf, pos, b, off, cnt);
        pos += cnt;
        return cnt;
    }

    /**
     * Reads bytes from this stream into the specified byte array,
     * starting at the given offset.
     *
     * <p> This method implements the general contract of the corresponding
     * <code>{@link java.io.InputStream#read(byte[], int, int) read}</code>
     * method of the <code>{@link java.io.InputStream}</code> class.
     *
     * @param      b     destination buffer.
     * @param      off   offset at which to start storing bytes.
     * @param      len   maximum number of bytes to read.
     * @return the number of bytes read, or <code>-1</code> if the end of
     *             the stream has been reached.
     * @exception IOException  if an I/O error occurs.
     */
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int n = read1(b, off, len);
        if (n <= 0)
            return n;
        while ((n < len) /* && (in.available() > 0) */) {
            int n1 = read1(b, off + n, len - n);
            if (n1 <= 0)
                break;
            n += n1;
        }
        return n;
    }

    /**
     * See the general contract of the <code>skip</code>
     * method of <code>InputStream</code>.
     *
     * @param      n   the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @exception IOException  if an I/O error occurs.
     */
    public synchronized long skip(long n) throws IOException {
        ensureOpen();
        if (n <= 0) {
            return 0;
        }
        long avail = count - pos;

        if (avail <= 0) {
            // If no mark position set then don't keep in buffer
            /*
                if (markpos <0) 
                    return in.skip(n);
             */

            // Fill in buffer to save bytes for reset
            fill();
            avail = count - pos;
            if (avail <= 0)
                return 0;
        }

        long skipped = Math.min(avail, n);
        pos += (int) skipped;
        return skipped;
    }

    /**
     * Returns the number of bytes that can be read from this input 
     * stream without blocking. 
     *
     * @return the number of bytes that can be read from this input
     *             stream without blocking.
     * @exception IOException  if an I/O error occurs.
     */
    public synchronized int available() throws IOException {
        ensureOpen();
        return (count - pos) + in_available();
    }

    private int in_available() {
        // XXX - overflow
        return (int) ((start + datalen) - (bufpos + count));
    }

    /**
     * See the general contract of the <code>mark</code>
     * method of <code>InputStream</code>.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     #reset()
     */
    public synchronized void mark(int readlimit) {
        marklimit = readlimit;
        markpos = pos;
    }

    /**
     * See the general contract of the <code>reset</code>
     * method of <code>InputStream</code>.
     * <p>
     * If <code>markpos</code> is <code>-1</code>
     * (no mark has been set or the mark has been
     * invalidated), an <code>IOException</code>
     * is thrown. Otherwise, <code>pos</code> is
     * set equal to <code>markpos</code>.
     *
     * @exception IOException  if this stream has not been marked or
     *               if the mark has been invalidated.
     * @see        #mark(int)
     */
    public synchronized void reset() throws IOException {
        ensureOpen();
        if (markpos < 0)
            throw new IOException("Resetting to invalid mark");
        pos = markpos;
    }

    /**
     * Tests if this input stream supports the <code>mark</code> 
     * and <code>reset</code> methods. The <code>markSupported</code> 
     * method of <code>SharedFileInputStream</code> returns 
     * <code>true</code>. 
     *
     * @return a <code>boolean</code> indicating if this stream type supports
     *          the <code>mark</code> and <code>reset</code> methods.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Closes this input stream and releases any system resources 
     * associated with the stream. 
     *
     * @exception IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        if (in == null)
            return;
        try {
            sf.close();
        } finally {
            sf = null;
            in = null;
            buf = null;
        }
    }

    /**
     * Return the current position in the InputStream, as an
     * offset from the beginning of the InputStream.
     *
     * @return the current position
     */
    public long getPosition() {
        if (in == null)
            throw new RuntimeException("Stream closed");
        return bufpos + pos - start;
    }

    /**
     * Return a new InputStream representing a subset of the data
     * from this InputStream, starting at <code>start</code> (inclusive)
     * up to <code>end</code> (exclusive).  <code>start</code> must be
     * non-negative.  If <code>end</code> is -1, the new stream ends
     * at the same place as this stream.  The returned InputStream
     * will also implement the SharedInputStream interface.
     *
     * @param   start   the starting position
     * @param   end     the ending position + 1
     * @return the new stream
     */
    public InputStream newStream(long start, long end) {
        if (in == null)
            throw new RuntimeException("Stream closed");
        if (start < 0)
            throw new IllegalArgumentException("start < 0");
        if (end == -1)
            end = datalen;
        return new SharedTmpFileInputStream(sf, this.start + (int) start,
                (int) (end - start), bufsize);
    }

    /**
     * Force this stream to close.
     */
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
