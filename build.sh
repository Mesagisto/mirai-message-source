compile(){
	./gradlew clean buildPlugin
	rm -rf packages
	mkdir -p packages
	mv build/mirai/*.mirai.jar packages/
	mv packages/*.jar packages/all.jar
}
extract(){
	rm -rf extract
	mkdir -p extract
	unzip packages/all.jar -d extract/ > /dev/null
}
linux-x86_64(){
  target=target/linux-x86_64
  name=linux-x86_64.jar

	mkdir -p $target/extract $target/tmp
	cp -r extract $target
	# rocksdb
	mv $target/extract/librocksdbjni-linux64.so $target/tmp
	rm $target/extract/librocksdbjni*
	mv $target/tmp/librocksdbjni-linux64.so $target/extract
	# webp
	mv $target/extract/native/Linux/x86_64/libwebp-imageio.so $target/tmp
	rm -rf  $target/extract/native
	mkdir -p $target/extract/native/Linux/x86_64
	mv $target/tmp/libwebp-imageio.so $target/extract/native/Linux/x86_64/libwebp-imageio.so
	# package
	jar -c -f packages/$name -C $target/extract/ .
	rm -rf $target
}
linux-x86(){
  target=target/linux-x86
  name=linux-x86.jar

	mkdir -p $target/extract $target/tmp
	cp -r extract $target
	# rocksdb
	mv $target/extract/librocksdbjni-linux32.so $target/tmp
	rm $target/extract/librocksdbjni*
	mv $target/tmp/librocksdbjni-linux32*.so $target/extract
	# webp
	mv $target/extract/native/Linux/x86/libwebp-imageio.so $target/tmp
	rm -rf  $target/extract/native
	mkdir -p $target/extract/native/Linux/x86
	mv $target/tmp/libwebp-imageio.so $target/extract/native/Linux/x86/libwebp-imageio.so
	# package
	jar -c -f packages/$name -C $target/extract/ .
	rm -rf $target
}
linux-aarch64(){
  target=target/linux-aarch64
  name=linux-aarch64.jar

	mkdir -p $target/extract $target/tmp
	cp -r extract $target
	# rocksdb
	mv $target/extract/librocksdbjni-linux-aarch64.so $target/tmp
	rm $target/extract/librocksdbjni*
	mv $target/tmp/*.so $target/extract
	# webp
	mv $target/extract/native/Linux/aarch64/libwebp-imageio.so $target/tmp
	rm -rf  $target/extract/native
	mkdir -p $target/extract/native/Linux/aarch64
	mv $target/tmp/libwebp-imageio.so $target/extract/native/Linux/aarch64/libwebp-imageio.so
	# package
	jar -c -f packages/$name -C $target/extract/ .
	rm -rf $target
}
windows-x86_64(){
  target=target/windows-x86_64
  name=windows-x86_64.jar

	mkdir -p $target/extract $target/tmp
	cp -r extract $target
	# rocksdb
	mv $target/extract/librocksdbjni-win64.dll $target/tmp
	rm $target/extract/librocksdbjni*
	mv $target/tmp/librocksdbjni-win64.dll $target/extract
	# webp
	mv $target/extract/native/Windows/x86_64/webp-imageio.dll $target/tmp
	rm -rf  $target/extract/native
	mkdir -p $target/extract/native/Windows/x86_64
	mv $target/tmp/webp-imageio.dll $target/extract/native/Windows/x86_64/webp-imageio.dll
	# package
	jar -c -f packages/$name -C $target/extract/ .
	rm -rf $target
}
osx-x86_64(){
  target=target/osx-x86_64
  name=osx-x86_64.jar

	mkdir -p $target/extract $target/tmp
	cp -r extract $target
	# rocksdb
	mv $target/extract/librocksdbjni-osx-x86_64.jnilib $target/tmp
	rm $target/extract/librocksdbjni*
	mv $target/tmp/librocksdbjni-osx-x86_64.jnilib $target/extract
	# webp
	mv $target/extract/native/Mac/x86_64/libwebp-imageio.dylib $target/tmp
	rm -rf  $target/extract/native
	mkdir -p $target/extract/native/Mac/x86_64
	mv $target/tmp/libwebp-imageio.dylib $target/extract/native/Mac/x86_64/libwebp-imageio.dylib
	# package
	jar -c -f packages/$name -C $target/extract/ .
	rm -rf $target
}
all_target(){
	linux-x86_64
	linux-x86
	linux-aarch64
	windows-x86_64
	osx-x86_64
	cd packages
	prefix=mirai-
	for files in $(ls *.jar)
		do mv $files $prefix$files
	done
	cd ..
}
clean(){
	rm -rf extract target
}
build(){
	compile
	extract
	all_target
	clean
}
build

