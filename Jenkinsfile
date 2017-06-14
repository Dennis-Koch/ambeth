#!groovy

timestamps {
    // Main Build
    node ('JAMA && LINUX'){

	    try {
	        // Mark the code checkout 'stage'....
	        stage ('Checkout') {
		        // Get code from the GitHub repository
		        checkout scm
			}
	        
	        // Mark the code build 'stage'....
	        stage ('Compile') {
	        	
	        	//check the version...not working right now
		        //def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
		        def originalV = version();
			    def major = originalV[0];
			    def minor = originalV[1];
			    def bugfix = originalV[2];
			    version = "${major}.${minor}.${bugfix}"
			    if (version) {
			       echo "Building version ${version}"
				}
		        
		        
		        // Run the maven build
		        withEnv(["PATH+MAVEN=${tool 'M3'}/bin","JAVA_HOME=${tool 'JDK8'}"]) {
		        	if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop') {
		        		sh 'echo Building ${env.BRANCH_NAME} branch...deploy is activated'
		            	
		            	// set version
		            	// Run the maven build this is a release that keeps the development version 
		  				// unchanged and uses Jenkins to provide the version number uniqueness
		  				//sh "mvn -DreleaseVersion=${version} -DdevelopmentVersion=${pom.version} -DpushChanges=false -DlocalCheckout=true -DpreparationGoals=initialize release:prepare release:perform -B"
		  				// set all version numbers
				        //sh "mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${version} -Dtycho.mode=maven"
		            	
		            	sh "mvn -B clean deploy -DskipTests -Dtycho.localArtifacts=ignore -Djambeth.path.test=""${env.WORKSPACE}/jambeth/jambeth-test"""
		            } else {
		            	sh 'echo Building NOT master or develop branch...NO deploy is being done'
		            	sh "mvn -B clean verify -DskipTests -Dtycho.localArtifacts=ignore -Djambeth.path.test=""${env.WORKSPACE}/jambeth/jambeth-test"""
		            }
		        }
		        
		        //timeout(time: 1, unit: 'MINUTES') {
			    //    input 'Publish?'
	  			//	stage 'Publish'
	  				// push the tags (alternatively we could have pushed them to a separate
	  				// git repo that we then pull from and repush... the latter can be 
	  				// helpful in the case where you run the publish on a different node
	  				//sh "git push ${pom.artifactId}-${version}"
	  			//}
	        }
	        
	        // Sonarqube analysis
	        stage('SonarQube analysis') {
	            withEnv(["PATH+MAVEN=${tool 'M3'}/bin","JAVA_HOME=${tool 'JDK8'}"]) {
	                withSonarQubeEnv('Sonarqube Server') {
	                    // requires SonarQube Scanner for Maven 3.2+
	                    def groupId = getGroupIdFromPom();
	                    def artifactId = getArtifactIdFromPom();
	                    echo "Sonarqube analysis for ${groupId}.${artifactId}"
	                    sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.projectName=${groupId}.${artifactId}"
	                }
	            }
	        }
		}
		catch (err) {
			echo 'Error: ' + err.getMessage()
			emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
			         replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
			         to: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
			                                 [$class: 'RequesterRecipientProvider']]))
			sh 'echo Sent email notification';
			currentBuild.result = 'FAILURE'
		}
    }
}

def version() {
	def matcher = readFile('pom.xml') =~ '<version>(.+)-.*</version>'
	matcher ? matcher[0][1].tokenize(".") : null
}

def getGroupIdFromPom() {
	def matcher = readFile('pom.xml') =~ '<groupId>(.+)</groupId>'
	matcher ? matcher[0][1] : null
 }

def getArtifactIdFromPom() {
	def matcher = readFile('pom.xml') =~ '<artifactId>(.+)</artifactId>'
	matcher ? matcher[0][1] : null
}
    
