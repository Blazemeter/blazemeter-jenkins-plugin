HOW TO ADD additional information to build:
1.{jenkins.build.number}
  - add -Djenkins.build.number=<build.number>
2. jenkins.git.commit}
  - add  -Djenkins.git.commit=$(git rev-parse --short HEAD)