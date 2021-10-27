all: linux-x86 linux-x86_64 windows-x86_64 mac-x86_64
compile: gradlew
	./gradlew build buildPlugin
	rm -rf packages
	mkdir -p packages
	mv build/mirai/*.jar packages/
	mv packages/*.mirai.jar packages/mirai-all.jar
extract: compile
	rm -rf extract
	mkdir -p extract
	unzip packages/mirai-all.jar -d extract/ > /dev/null
clean:
	rm -rf target extract
linux-x86_64: target := target/linux-x86_64
linux-x86_64: name := mirai-linux-x86_64.jar
linux-x86_64: extract
	mkdir -p $(target)/extract $(target)/tmp
	cp -r extract $(target)
	# rocksdb
	mv $(target)/extract/librocksdbjni-linux64.so $(target)/tmp
	rm $(target)/extract/librocksdbjni*
	mv $(target)/tmp/librocksdbjni-linux64.so $(target)/extract
	# webp
	mv $(target)/extract/native/Linux/x86_64/libwebp-imageio.so $(target)/tmp
	rm -rf  $(target)/extract/native
	mkdir -p $(target)/extract/native/Linux/x86_64
	mv $(target)/tmp/libwebp-imageio.so $(target)/extract/native/Linux/x86_64/libwebp-imageio.so
	# package
	jar -c -f packages/$(name) -C $(target)/extract/ .
	rm -rf $(target)
linux-x86: target := target/linux-x86
linux-x86: name := mirai-linux-x86.jar
linux-x86: extract
	mkdir -p $(target)/extract $(target)/tmp
	cp -r extract $(target)
	# rocksdb
	mv $(target)/extract/librocksdbjni-linux32.so $(target)/tmp
	rm $(target)/extract/librocksdbjni*
	mv $(target)/tmp/librocksdbjni-linux32*.so $(target)/extract
	# webp
	mv $(target)/extract/native/Linux/x86/libwebp-imageio.so $(target)/tmp
	rm -rf  $(target)/extract/native
	mkdir -p $(target)/extract/native/Linux/x86
	mv $(target)/tmp/libwebp-imageio.so $(target)/extract/native/Linux/x86/libwebp-imageio.so
	# package
	jar -c -f packages/$(name) -C $(target)/extract/ .
	rm -rf $(target)
windows-x86_64: target := target/windows-x86_64
windows-x86_64: name := mirai-windows-x86_64.jar
windows-x86_64: extract
	mkdir -p $(target)/extract $(target)/tmp
	cp -r extract $(target)
	# rocksdb
	mv $(target)/extract/librocksdbjni-win64.dll $(target)/tmp
	rm $(target)/extract/librocksdbjni*
	mv $(target)/tmp/librocksdbjni-win64.dll $(target)/extract
	# webp
	mv $(target)/extract/native/Windows/x86_64/webp-imageio.dll $(target)/tmp
	rm -rf  $(target)/extract/native
	mkdir -p $(target)/extract/native/Windows/x86_64
	mv $(target)/tmp/webp-imageio.dll $(target)/extract/native/Windows/x86_64/webp-imageio.dll
	# package
	jar -c -f packages/$(name) -C $(target)/extract/ .
	rm -rf $(target)
mac-x86_64: target := target/mac-x86_64
mac-x86_64: name := mirai-mac-x86_64.jar
mac-x86_64: extract
	mkdir -p $(target)/extract $(target)/tmp
	cp -r extract $(target)
	# rocksdb
	mv $(target)/extract/librocksdbjni-osx.jnilib $(target)/tmp
	rm $(target)/extract/librocksdbjni*
	mv $(target)/tmp/librocksdbjni-osx.jnilib $(target)/extract
	# webp
	mv $(target)/extract/native/Mac/x86_64/libwebp-imageio.dylib $(target)/tmp
	rm -rf  $(target)/extract/native
	mkdir -p $(target)/extract/native/Mac/x86_64
	mv $(target)/tmp/libwebp-imageio.dylib $(target)/extract/native/Mac/x86_64/libwebp-imageio.dylib
	# package
	jar -c -f packages/$(name) -C $(target)/extract/ .
	rm -rf $(target)

