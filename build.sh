build(){
	./gradlew clean buildPlugin
	rm -rf packages
	mkdir -p packages
	mv build/mirai/*.mirai2.jar packages/mesagisto.mirai2.jar
}
build

