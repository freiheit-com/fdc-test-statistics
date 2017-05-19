FROM openjdk:8

ARG VERSION
RUN echo "Building version ${VERSION}"

ADD public/ /ui/
ADD target/fdc-test-statistics-$VERSION-standalone.jar /app.jar

EXPOSE 3001

ENTRYPOINT ["java","-jar","/app.jar"]
