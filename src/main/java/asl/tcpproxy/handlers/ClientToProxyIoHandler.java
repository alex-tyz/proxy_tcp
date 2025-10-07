
package asl.tcpproxy.handlers;

import java.net.SocketAddress;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientToProxyIoHandler extends AbstractProxyIoHandler {

	private final static Logger log = LoggerFactory.getLogger(ClientToProxyIoHandler.class);

	private final ServerToProxyIoHandler connectorHandler = new ServerToProxyIoHandler();

	private final IoConnector connector;

	private final SocketAddress remoteAddress;

	public ClientToProxyIoHandler(IoConnector connector, SocketAddress remoteAddress) {

		this.connector = connector;
		this.remoteAddress = remoteAddress;
		connector.setHandler(connectorHandler);
	}

	@Override
	public void sessionOpened(final IoSession session) throws Exception {

		connector.connect(remoteAddress).addListener(new IoFutureListener<ConnectFuture>() {
			public void operationComplete(ConnectFuture future) {
				try {
					future.getSession().setAttribute(OTHER_IO_SESSION, session);
					session.setAttribute(OTHER_IO_SESSION, future.getSession());
					IoSession session2 = future.getSession();
					session2.resumeRead();
					session2.resumeWrite();

					if (log.isInfoEnabled()) {
						log.info(String.format("Connection is done! from %s to %s for:",
								session.getRemoteAddress(),
								remoteAddress,
								OTHER_IO_SESSION));

					}

				} catch (RuntimeIoException e) {
					if (log.isErrorEnabled()) {
						log.error("Connect failed for " + OTHER_IO_SESSION, e);
					}
					session.closeNow();
				} finally {
					session.resumeRead();
					session.resumeWrite();
				}
			}
		});
	}
}
