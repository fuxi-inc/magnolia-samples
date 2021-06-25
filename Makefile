.ONESHELL:
.PHONY: build
build:
	gradle clean shadowJar --refresh-dependencies
	echo "Build phrase done."

.PHONY: publish
publish:
	gradle clean publish
	echo "Publish done"
