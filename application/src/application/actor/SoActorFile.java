package application.actor;

import jscenegraph.database.inventor.nodes.SoFile;

public class SoActorFile extends SoFile {
	
	Actor actor;

	public SoActorFile(Actor actor) {
		this.actor = actor;
	}

	public Actor getActor() {
		return actor;
	}
}
