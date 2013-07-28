package org.mailster.smtp.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

import javax.mail.util.SharedByteArrayInputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.mailster.smtp.util.SharedTmpFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SMTP protocol decoder context is used when a client 
 * command is split among multiple network packets.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPDecoderContext 
{
	private static final Logger LOG = LoggerFactory.getLogger(SMTPDecoderContext.class);
	
	private final CharsetDecoder charsetDecoder;
    private IoBuffer buf;
    private int matchCount = 0;
    private int overflowPosition = 0;
	private boolean thresholdReached = false;        
	private boolean dataMode = false;
	
	/** If we switch to file output, this is the file. */
	private File outFile;

	/** If we switch to file output, this is the stream to write to the file. */ 
	private FileOutputStream stream;
	
	private SMTPDecoder decoder;
	
    protected SMTPDecoderContext(SMTPDecoder decoder) 
    {
    	this.decoder = decoder;
    	charsetDecoder = decoder.getCharset().newDecoder();
        buf = IoBuffer.allocate(decoder.getMaxLineLength()).setAutoExpand(true);
    }

	/** */
    private static byte[] asArray(IoBuffer b) 
    {
    	int len = b.remaining();
    	byte[] array = new byte[len];
    	b.get(array, 0, len);
	    
	    return array;
    }
    
    /** */
    protected CharsetDecoder getDecoder() 
    {
        return charsetDecoder;
    }

    /** */
    protected IoBuffer getBuffer() 
    {
        return buf;
    }
    
    /** */
    private void compactBuffer() 
    {
    	if (dataMode && buf.capacity() > decoder.getMaxLineLength())
    		buf = IoBuffer.allocate(decoder.getMaxLineLength()).setAutoExpand(true);
    	else
    		buf.clear();
    }
    
    /** */
    protected int getOverflowPosition() 
    {
        return overflowPosition;
    }
    
    /** */
    protected int getMatchCount() 
    {
        return matchCount;
    }

    /** */
    protected void setMatchCount(int matchCount) 
    {
        this.matchCount = matchCount;
    }
    
    /** */
    protected void reset() throws IOException 
    {
        overflowPosition = 0;
        matchCount = 0;
        charsetDecoder.reset();
        if (thresholdReached)
        {
        	thresholdReached = false;
        	compactBuffer();
        	closeOutputStream();
        }
    }
    
    /** */
    protected void write(IoBuffer b) 
    	throws IOException
    {
		if (dataMode)
			write(asArray(b));
		else
			append(b);
    }
    
    /** */
	private void write(byte[] src) 
		throws IOException
	{
		int predicted = this.thresholdReached ? 0 : this.buf.position() + src.length;
		
		// Checks whether reading count bytes would cross the limit.
		if (this.thresholdReached || predicted > decoder.getThreshold())
		{
			// If previously hit, then use the stream.
			if (!this.thresholdReached)
				thresholdReached();
			
			this.stream.write(src);
		}
		else
			this.buf.put(src);
	}
	
	/**
	 * Called when the threshold is about to be exceeded. Once called, it
	 * won't be called again for the current data transfer.
	 */
	private void thresholdReached() 
		throws IOException
	{
		this.outFile = File.createTempFile(SMTPDecoder.TMPFILE_PREFIX, 
				SMTPDecoder.TMPFILE_SUFFIX);

		LOG.debug("Writing message to file : {}", outFile.getAbsolutePath());
		
		this.stream = new FileOutputStream(this.outFile);
		this.buf.flip();
		this.stream.write(asArray(this.buf));
		this.thresholdReached = true;
		this.buf.clear();
		LOG.debug("ByteBuffer written to stream");
	}
	
	/** */
	protected void closeOutputStream() throws IOException
	{
		if (this.stream != null)
		{
			this.stream.flush();
			this.stream.close();
			LOG.debug("Temp file writing achieved - closing stream");
		}
	}
	
	/** */
	protected InputStream getNewInputStream() throws IOException
	{		
		if (this.thresholdReached)
			return new SharedTmpFileInputStream(this.outFile);
		else
			return new SharedByteArrayInputStream(asArray(this.buf));
	}
	
	/** */
    private void append(IoBuffer in) throws CharacterCodingException 
    {
        if (overflowPosition != 0) 
            discard(in);
        else 
        {
        	int pos = buf.position();
        	if ((pos + in.remaining()) > decoder.getMaxLineLength()) 
        	{
                overflowPosition = pos;
                buf.clear();
                discard(in);
        	} 
        	else 
        		this.buf.put(in);
        }
    }

    /** */
    private void discard(IoBuffer in) 
    {
        if (Integer.MAX_VALUE < (overflowPosition + in.remaining()) ) 
            overflowPosition = Integer.MAX_VALUE;
        else 
            overflowPosition += in.remaining();
        
        in.position(in.limit());
    }

    /** */
    public void setDataMode(boolean dataMode) 
    {
		this.dataMode = dataMode;
	}
}