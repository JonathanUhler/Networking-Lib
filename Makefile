SRC_DIR      := src
JNET_SRC_DIR := $(SRC_DIR)/jnet
PNET_SRC_DIR := $(SRC_DIR)/pnet
OBJ_DIR      := obj
BIN_DIR      := bin
JAVADOC_DIR  := docs/javadoc
PYDOC_DIR    := docs/pydoc

TEST_KEYSTORE_PASSWORD := changeit
TEST_KEYSTORE_FILE := keystore


.PHONY: compile_jnet \
	jar_jnet     \
	jnet         \
	gen_jks      \
	test_jnet    \
	pnet         \
	gen_pks      \
	test_pnet    \
	javadoc      \
	javadoc_dir  \
	pydoc        \
	pydoc_dir    \
	docs         \
	obj_dir      \
	bin_dir      \
	clean

# Compiles the source of the Java networking library into .class files
compile_jnet: obj_dir
	javac -d obj $(shell find $(JNET_SRC_DIR) -name "*.java")

# Creates a JAR file from the class files generated with the compile_jnet target
jar_jnet: compile_jnet bin_dir
	jar cf $(BIN_DIR)/jnet.jar -C $(OBJ_DIR) .

# An alias for jar_jnet
jnet: jar_jnet

# Generates a testing-only, self-signed key and certificate for testing jnet.secure
gen_jks:
	@keytool                                             \
		-genkey                                      \
		-alias localhost                             \
		-keyalg rsa                                  \
		-dname "cn=, ou=, o=, l=, s=, c="            \
		-validity 1                                  \
		-keystore "$(OBJ_DIR)/$(TEST_KEYSTORE_FILE)" \
		-storepass "$(TEST_KEYSTORE_PASSWORD)"
	@keytool                                             \
		-export                                      \
		-alias localhost                             \
		-file "$(OBJ_DIR)/$(TEST_KEYSTORE_FILE).pem" \
		-keystore "$(OBJ_DIR)/$(TEST_KEYSTORE_FILE)" \
		-rfc                                         \
		-storepass "$(TEST_KEYSTORE_PASSWORD)"
	@keytool                                                \
		-import                                         \
		-alias localhost                                \
		-file "$(OBJ_DIR)/$(TEST_KEYSTORE_FILE).pem"    \
		-keystore "$(OBJ_DIR)/$(TEST_KEYSTORE_FILE).ts" \
		-storepass "$(TEST_KEYSTORE_PASSWORD)"

# Runs unit tests for jnet
test_jnet: jnet gen_jks
	javac -cp '.:$(SRC_DIR)/lib/*:$(BIN_DIR)/*' -d $(OBJ_DIR)/tests \
		$(shell find tests/jnet -name '*.java')
	java -cp '.:$(SRC_DIR)/lib/*:$(OBJ_DIR)/tests:$(BIN_DIR)/*'            \
		-Djavax.net.ssl.keyStore=$(OBJ_DIR)/$(TEST_KEYSTORE_FILE)      \
		-Djavax.net.ssl.keyStorePassword=$(TEST_KEYSTORE_PASSWORD)     \
		-Djavax.net.ssl.trustStore=$(OBJ_DIR)/$(TEST_KEYSTORE_FILE).ts \
		-Djavax.net.ssl.trustStorePassword=$(TEST_KEYSTORE_PASSWORD)   \
		org.junit.runner.JUnitCore                                     \
		TestBytes TestCRC TestHeader TestNetworking TestSecureNetworking

# Creates a TAR archive with all the necessary source for installing pnet locally with pip
pnet: bin_dir
	mkdir -p $(BIN_DIR)/pnet
	rsync -r --exclude "*~" $(PNET_SRC_DIR) $(BIN_DIR)
	tar -czf $(BIN_DIR)/pnet.tar.gz -C $(BIN_DIR) pnet

# Generates a testing-only, self-signed key and certificate for testing pnet.secure
gen_pks: obj_dir
	@openssl req                                           \
		-newkey rsa:2048                               \
		-nodes                                         \
		-keyout "$(OBJ_DIR)/$(TEST_KEYSTORE_FILE).key" \
		-x509                                          \
		-subj "/CN=localhost"                          \
		-days 1                                        \
		-out "$(OBJ_DIR)/$(TEST_KEYSTORE_FILE).crt"

# Runs unit tests for pnet
test_pnet: pnet gen_pks
	python3 -m venv $(BIN_DIR)/venv
	. $(BIN_DIR)/venv/bin/activate
	pip3 install ./$(BIN_DIR)/pnet pytest
	PNET_KEYFILE="$(OBJ_DIR)/$(TEST_KEYSTORE_FILE).key" \
	PNET_CERTFILE="$(OBJ_DIR)/$(TEST_KEYSTORE_FILE).crt" \
	python3 -m pytest tests/pnet

# Builds javadocs for jnet
javadoc: javadoc_dir
	javadoc $(shell find $(JNET_SRC_DIR) -name "*.java") -d $(JAVADOC_DIR)

# Helper target to create a directory for storing javadocs
javadoc_dir:
	mkdir -p $(JAVADOC_DIR)

# Creates html docs for pnet
pydoc: pydoc_dir
	@echo "pydoc not yet implemented"

# Helper target to create a directory for storing pnet docs
pydoc_dir:
	mkdir -p $(PYDOC_DIR)

# A wrapper to build jnet and pnet html docs
docs: javadoc pydoc

# Helper target to create the obj directory
obj_dir:
	mkdir -p $(OBJ_DIR)

# Helper target to create the bin directory
bin_dir:
	mkdir -p $(BIN_DIR)

# Removes all generated and artifact files
clean:
	@rm -rf $(BIN_DIR) $(OBJ_DIR) $(JAVADOC_DIR) $(PYDOC_DIR)
