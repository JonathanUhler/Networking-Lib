JNET_SRC_DIR := src/jnet
PNET_SRC_DIR := src/pnet
OBJ_DIR      := obj
REL_DIR      := rel
JAVADOC_DIR  := docs/javadoc
PYDOC_DIR    := docs/pydoc


.PHONY: compile_jnet \
	jar_jnet     \
	jnet         \
	pnet         \
	javadoc      \
	javadoc_dir  \
	pydoc        \
	pydoc_dir    \
	docs         \
	obj_dir      \
	rel_dir      \
	clean

compile_jnet: obj_dir
	javac -d obj $(shell find $(JNET_SRC_DIR) -name "*.java")

jar_jnet: rel_dir
	jar cf $(REL_DIR)/jnet.jar -C $(OBJ_DIR) .

jnet: compile_jnet jar_jnet

pnet: rel_dir
	mkdir -p $(REL_DIR)/pnet
	rsync -r --exclude "*~" $(PNET_SRC_DIR) $(REL_DIR)
	tar -czf $(REL_DIR)/pnet.tar.gz -C $(REL_DIR) pnet

javadoc: javadoc_dir
	javadoc $(shell find $(JNET_SRC_DIR) -name "*.java") \
		-d $(JAVADOC_DIR)

javadoc_dir:
	mkdir -p $(JAVADOC_DIR)

pydoc: pydoc_dir
	@echo "pydoc not yet implemented"

pydoc_dir:
	mkdir -p $(PYDOC_DIR)

docs: javadoc pydoc

obj_dir:
	mkdir -p $(OBJ_DIR)

rel_dir:
	mkdir -p $(REL_DIR)

clean:
	@rm -rf $(OBJ_DIR) $(JAVADOC_DIR)
