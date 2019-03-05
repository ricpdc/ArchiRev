mvn deploy:deploy-file -DgroupId=com.mxgraph -DartifactId=jgraphx -Dversion=3.9.6 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=./src/main/resources/lib/jgraphx.jar

mvn deploy:deploy-file -DgroupId=com.archimatetool -DartifactId=com.archimatetool.model -Dversion=4.2.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=./src/main/resources/lib/com.archimatetool.model.jar


pause
