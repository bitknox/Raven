package dk.itu.raven.join;

import java.awt.Rectangle;

public class EmptyRavenJoin extends AbstractRavenJoin {

	public EmptyRavenJoin() {
		super(new Rectangle());

	}

	@Override
	protected AbstractJoinResult joinImplementation(IRasterFilterFunction function) {
		// TODO Auto-generated method stub
		return new JoinResult();
	}

}