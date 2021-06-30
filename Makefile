.ONESHELL:
.PHONY: build
build:
	gradle clean build --refresh-dependencies
	echo "Build phrase done."

.PHONY: run
run: build
	gradle run