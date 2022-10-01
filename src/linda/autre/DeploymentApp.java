package linda.autre;

import linda.Linda;

/** YOU MUST INHERIT FROM THIS CLASS, DECLARE A CONSTRUCTOR WITH ONLY ONE PARAMETER LINDA AND WITH
 * SUPER(LINDA) AS THE FIRST LINE, USE LINDA AS "linda" AND FINALLY OVERRIDE START to deploy your application on our platform */
public abstract class DeploymentApp {

	protected Linda linda;
	
	public DeploymentApp(Linda linda) {
		this.linda = linda;
	}
	
	public abstract void start(String[] args);

	
}
