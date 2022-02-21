package linda.server;

import java.rmi.Naming;
import java.util.Collection;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements ILindaClient, Linda {
	LindaServer server;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        try {
			this.server = (LindaServer) Naming.lookup(serverURI);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	@Override
	public void write(Tuple t) {
		try {
			this.server.write(t.deepclone());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Tuple take(Tuple template) {
		try {
			return this.server.take(template);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple read(Tuple template) {
		try {
			return this.server.read(template);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple tryTake(Tuple template) {
		try {
			return this.server.tryTake(template);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple tryRead(Tuple template) {
		try {
			return this.server.tryRead(template);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		try {
			return this.server.takeAll(template);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		try {
			return this.server.readAll(template);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		try {
			this.server.eventRegister(mode, timing, template, new RemoteCallback(callback));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void debug(String prefix) {
		System.out.println(prefix);
	}
}
