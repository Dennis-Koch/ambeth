#!groovy

library("jenkins-library")

timestamps {
  // Main Build
	node ('JAMA && WINDOWS'){
		try {
			
			horizon.checkout()
			
			horizon.buildMaven( updateSnapshots:false )
			
			sonar.buildSonar()
			
		} catch (Exception err) {
			
			horizon.handleError(err)
		}
	}

	node ('JAMA && LINUX'){
		try {
			
			horizon.checkout()
			
			maven.exec("test")
			
		} catch (Exception err) {
			
			horizon.handleError(err)
		}
	}

}