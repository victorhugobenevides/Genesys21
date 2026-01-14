# Estágio 1: Build
FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Construímos apenas o módulo server e suas dependências (como o shared)
RUN ./gradlew :server:installDist --no-daemon

# Estágio 2: Runtime
FROM openjdk:17-slim
EXPOSE 8080
RUN mkdir /app
# Copiamos a instalação gerada no estágio anterior
COPY --from=build /home/gradle/src/server/build/install/server /app/
WORKDIR /app/bin

# Executa o servidor
ENTRYPOINT ["./server"]
