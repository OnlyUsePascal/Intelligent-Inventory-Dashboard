include .env

DOCKB=docker compose build
DOCKU=docker compose up --build --detach 
MVNW=./mvnw
BUILD_DIR=./target
MODULE=inventory-0.0.1-SNAPSHOT

dock.build:
	$(DOCKB) inventory

dock.run:
	$(DOCKU) inventory

app.build:
	$(MVNW) clean package

app.build.no_test:
	$(MVNW) clean package -DskipTests
	
app.run: app.build
	java -jar $(BUILD_DIR)/$(MODULE).jar
