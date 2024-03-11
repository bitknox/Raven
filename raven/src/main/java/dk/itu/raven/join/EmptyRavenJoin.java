package dk.itu.raven.join;

public class EmptyRavenJoin extends AbstractRavenJoin {

	@Override
	protected IJoinResult joinImplementation(IRasterFilterFunction function) {
		return new JoinResult();
	}

}