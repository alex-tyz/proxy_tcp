
package asl.tcpproxy.handlers;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProxyIoHandler extends IoHandlerAdapter {
	public static final String OTHER_IO_SESSION = AbstractProxyIoHandler.class.getName() + ".OtherIoSession";

	private final static Logger log = LoggerFactory.getLogger(AbstractProxyIoHandler.class);

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		session.suspendRead();
		session.suspendWrite();
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if (log.isWarnEnabled()) {
			log.warn("Closing session:" + session.getAttribute(OTHER_IO_SESSION));
		}
		if (session.getAttribute(OTHER_IO_SESSION) != null) {
			IoSession sess = (IoSession) session.getAttribute(OTHER_IO_SESSION);
			sess.setAttribute(OTHER_IO_SESSION, null);
			sess.closeOnFlush();
			session.setAttribute(OTHER_IO_SESSION, null);
		}
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		IoBuffer rb = (IoBuffer) message;
		IoBuffer wb = IoBuffer.allocate(rb.remaining());
		rb.mark();
		wb.put(rb);
		wb.flip();
		((IoSession) session.getAttribute(OTHER_IO_SESSION)).write(wb);
		rb.reset();
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

		if (log.isErrorEnabled()) {
			log.error("Exception Caught for " + session.getAttribute(OTHER_IO_SESSION).toString());
			log.error(cause.toString());
		}

		super.exceptionCaught(session, cause);

	}
}
