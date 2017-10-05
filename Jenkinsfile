#!groovy

timestamps {
    // Main Build
    node ('JAMA'){

	    try {
			stage ('Clean') {
	       		step([$class: 'WsCleanup'])
			}
	        stage ('Checkout') {
		        checkout scm
			}
	        
			def deployOrVerify = 'install';
			def deployOrVerifyLabelPrefix = 'Verify';
			def profile = ' -P default';
			def localRepo = ' -Dmaven.repo.local=${env.WORKSPACE}/.m2';
			if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop') {
				deployOrVerify = 'deploy';
				deployOrVerifyLabelPrefix = 'Deploy';
				profile = ' -P default';
				localRepo = '';
			}
			
	        // Mark the code build 'stage'....
	        stage ("${deployOrVerifyLabelPrefix}") {
	        	
		        // Run the maven build
		        withEnv(["JAVA_HOME=${tool 'JDK8'}","PATH+MAVEN=${tool 'M3'}/bin","PATH+JAVA=${tool 'JDK8'}/bin","PATH+INKSCAPE=${env.WORKSPACE}/doc/reference-manual/target/inkscape/inkscape","PATH+MIKTEX=${env.WORKSPACE}/doc/reference-manual/target/miktex/miktex/miktex/bin/x64"]) {
		            
	            	sh "mvn -B clean ${deployOrVerify}${profile} -DskipTests -Dtycho.localArtifacts=ignore -Dpmd.skip=true -Dfindbugs.skip=true -Dcheckstyle.skip=true${localRepo} -Djambeth.path.test=${env.WORKSPACE}/jambeth/jambeth-test"
		        }
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
			sh 'Sent email notification';
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