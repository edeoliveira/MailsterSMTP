package org.mailster.smtp.core;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.BufferDataException;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;
import org.apache.mina.filter.codec.textline.TextLineEncoder;
import org.mailster.smtp.SMTPServerConfig;

/**
 * A {@link ProtocolCodecFactory} that performs encoding and decoding between
 * a text line data and a Java string object.  This codec is useful especially
 * when you work with a text-based protocols such as SMTP and IMAP.
 *
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 471457 $, $Date: 2006-11-06 01:44:10 +0900 (월, 06 11월 2006) $
 */
public class SMTPCodecFactory implements ProtocolCodecFactory
{
	private final TextLineEncoder encoder;

	private final SMTPDecoder decoder;

	/**
	 * Creates a new instance using the configuration.
	 */
	public SMTPCodecFactory(SMTPServerConfig config)
	{
		this(config.getCharset(), config.getDataDeferredSize());
	}
	
	/**
	 * Creates a new instance using the configuration.
	 */
	protected SMTPCodecFactory(Charset charset, int dataDeferredSize)
	{
		encoder = new TextLineEncoder(charset, LineDelimiter.CRLF);
		decoder = new SMTPDecoder(charset, dataDeferredSize);
	}

	public ProtocolEncoder getEncoder(IoSession session)
	{
		return encoder;
	}

	public ProtocolDecoder getDecoder(IoSession session)
	{
		return decoder;
	}

	/**
	 * Returns the allowed maximum size of the encoded line.
	 * If the size of the encoded line exceeds this value, the encoder
	 * will throw a {@link IllegalArgumentException}.  The default value
	 * is {@link Integer#MAX_VALUE}.
	 * <p>
	 * This method does the same job with {@link TextLineEncoder#getMaxLineLength()}.
	 */
	public int getEncoderMaxLineLength()
	{
		return encoder.getMaxLineLength();
	}

	/**
	 * Sets the allowed maximum size of the encoded line.
	 * If the size of the encoded line exceeds this value, the encoder
	 * will throw a {@link IllegalArgumentException}.  The default value
	 * is {@link Integer#MAX_VALUE}.
	 * <p>
	 * This method does the same job with {@link TextLineEncoder#setMaxLineLength(int)}.
	 */
	public void setEncoderMaxLineLength(int maxLineLength)
	{
		encoder.setMaxLineLength(maxLineLength);
	}

	/**
	 * Returns the allowed maximum size of the line to be decoded.
	 * If the size of the line to be decoded exceeds this value, the
	 * decoder will throw a {@link BufferDataException}.  The default
	 * value is <tt>1024</tt> (1KB).
	 * <p>
	 * This method does the same job with {@link TextLineDecoder#getMaxLineLength()}.
	 */
	public int getDecoderMaxLineLength()
	{
		return decoder.getMaxLineLength();
	}

	/**
	 * Sets the allowed maximum size of the line to be decoded.
	 * If the size of the line to be decoded exceeds this value, the
	 * decoder will throw a {@link BufferDataException}.  The default
	 * value is <tt>1024</tt> (1KB).
	 * <p>
	 * This method does the same job with {@link TextLineDecoder#setMaxLineLength(int)}.
	 */
	public void setDecoderMaxLineLength(int maxLineLength)
	{
		decoder.setMaxLineLength(maxLineLength);
	}
}
