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
	javadoc      \
	javadoc_dir  \
	pydoc        \
	pydoc_dir    \
	docs         \
	obj_dir      \
	bin_dir      \
	clean

compile_jnet: obj_dir
	javac -d obj $(shell find $(JNET_SRC_DIR) -name "*.java")

jar_jnet: bin_dir
	jar cf $(BIN_DIR)/jnet.jar -C $(OBJ_DIR) .

jnet: compile_jnet jar_jnet

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

pnet: bin_dir
	mkdir -p $(BIN_DIR)/pnet
	rsync -r --exclude "*~" $(PNET_SRC_DIR) $(BIN_DIR)
	tar -czf $(BIN_DIR)/pnet.tar.gz -C $(BIN_DIR) pnet

test_pnet: pnet
	python3 -m venv $(BIN_DIR)/venv
	. $(BIN_DIR)/venv/bin/activate
	pip3 install ./$(BIN_DIR)/pnet pytest
	python3 -m pytest tests/pnet

javadoc: javadoc_dir
	javadoc $(shell find $(JNET_SRC_DIR) -name "*.java") -d $(JAVADOC_DIR)

javadoc_dir:
	mkdir -p $(JAVADOC_DIR)

pydoc: pydoc_dir
	@echo "pydoc not yet implemented"

pydoc_dir:
	mkdir -p $(PYDOC_DIR)

docs: javadoc pydoc

obj_dir:
	mkdir -p $(OBJ_DIR)

bin_dir:
	mkdir -p $(BIN_DIR)

clean:
	@rm -rf $(BIN_DIR) $(OBJ_DIR) $(JAVADOC_DIR)
