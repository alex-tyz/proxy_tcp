
package asl.tcpproxy.filters;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.firewall.Subnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link IoFilter} which allows connections from whitelisted remote address.
 * 
 * Was slightly changed standard MINA BlacklistFilter by
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 * @org.apache.xbean.XBean
 */
public class WhitelistFilter extends IoFilterAdapter {
	/** The list of blocked addresses */
	private final List<Subnet> blacklist = new CopyOnWriteArrayList<Subnet>();

	private final static Logger LOGGER = LoggerFactory
			.getLogger(WhitelistFilter.class);

	/**
	 * Sets the addresses to be blacklisted.
	 *
	 * NOTE: this call will remove any previously blacklisted addresses.
	 *
	 * @param addresses
	 *            an array of addresses to be blacklisted.
	 */
	public void setBlacklist(InetAddress[] addresses) {
		if (addresses == null) {
			throw new IllegalArgumentException("addresses");
		}

		blacklist.clear();

		for (int i = 0; i < addresses.length; i++) {
			InetAddress addr = addresses[i];
			block(addr);
		}
	}

	/**
	 * Sets the subnets to be blacklisted.
	 *
	 * NOTE: this call will remove any previously blacklisted subnets.
	 *
	 * @param subnets
	 *            an array of subnets to be blacklisted.
	 */
	public void setSubnetBlacklist(Subnet[] subnets) {
		if (subnets == null) {
			throw new IllegalArgumentException("Subnets must not be null");
		}

		blacklist.clear();

		for (Subnet subnet : subnets) {
			block(subnet);
		}
	}

	/**
	 * Sets the addresses to be blacklisted.
	 *
	 * NOTE: this call will remove any previously blacklisted addresses.
	 *
	 * @param addresses
	 *            a collection of InetAddress objects representing the addresses
	 *            to be blacklisted.
	 * @throws IllegalArgumentException
	 *             if the specified collections contains non-{@link InetAddress}
	 *             objects.
	 */
	public void setBlacklist(Iterable<InetAddress> addresses) {
		if (addresses == null) {
			throw new IllegalArgumentException("addresses");
		}

		blacklist.clear();

		for (InetAddress address : addresses) {
			block(address);
		}
	}

	/**
	 * Sets the subnets to be blacklisted.
	 *
	 * NOTE: this call will remove any previously blacklisted subnets.
	 *
	 * @param subnets
	 *            an array of subnets to be blacklisted.
	 */
	public void setSubnetBlacklist(Iterable<Subnet> subnets) {
		if (subnets == null) {
			throw new IllegalArgumentException("Subnets must not be null");
		}

		blacklist.clear();

		for (Subnet subnet : subnets) {
			block(subnet);
		}
	}

	/**
	 * Blocks the specified endpoint.
	 */
	public void block(InetAddress address) {
		if (address == null) {
			throw new IllegalArgumentException(
					"Adress to block can not be null");
		}

		block(new Subnet(address, 32));
	}

	/**
	 * Blocks the specified subnet.
	 */
	public void block(Subnet subnet) {
		if (subnet == null) {
			throw new IllegalArgumentException("Subnet can not be null");
		}

		blacklist.add(subnet);
	}

	/**
	 * Unblocks the specified endpoint.
	 */
	public void unblock(InetAddress address) {
		if (address == null) {
			throw new IllegalArgumentException(
					"Adress to unblock can not be null");
		}

		unblock(new Subnet(address, 32));
	}

	/**
	 * Unblocks the specified subnet.
	 */
	public void unblock(Subnet subnet) {
		if (subnet == null) {
			throw new IllegalArgumentException("Subnet can not be null");
		}

		blacklist.remove(subnet);
	}

	@Override
	public void sessionCreated(NextFilter nextFilter, IoSession session) {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.sessionCreated(session);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void sessionOpened(NextFilter nextFilter, IoSession session)
			throws Exception {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.sessionOpened(session);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void sessionClosed(NextFilter nextFilter, IoSession session)
			throws Exception {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.sessionClosed(session);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void sessionIdle(NextFilter nextFilter, IoSession session,
			IdleStatus status) throws Exception {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.sessionIdle(session, status);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.messageReceived(session, message);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void messageSent(NextFilter nextFilter, IoSession session,
			WriteRequest writeRequest) throws Exception {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.messageSent(session, writeRequest);
		} else {
			blockSession(session);
		}
	}

	private void blockSession(IoSession session) {
		LOGGER.warn("Remote address in the blacklist; closing.");
		session.close(true);
	}

	// CHANGED!!!!!
	// only change is to invert result of this method!
	private boolean isBlocked(IoSession session) {
		SocketAddress remoteAddress = session.getRemoteAddress();

		if (remoteAddress instanceof InetSocketAddress) {
			InetAddress address = ((InetSocketAddress) remoteAddress)
					.getAddress();

			// check all subnets
			for (Subnet subnet : blacklist) {
				if (subnet.inSubnet(address)) {
					return false;
				}
			}
		}

		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn(String.format("The connection has been disallowed for: %s",
					remoteAddress));
		}

		return true;
	}
}
