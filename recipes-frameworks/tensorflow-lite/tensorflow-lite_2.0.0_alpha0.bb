DESCRIPTION = "TensorFlow Lite C++ Library"
LICENSE = "Apache-2.0"

LIC_FILES_CHKSUM = "file://LICENSE;md5=01e86893010a1b87e69a213faa753ebd"

SRC_URI = " https://github.com/tensorflow/tensorflow/archive/v${PV}-${PR}.tar.gz "
SRC_URI[md5sum] = "a26886611105d3399c2a5985fe14d904"
SRC_URI[sha256sum] = "01eea2cc36439ee2f69e3f843673f0bbe6c5e30d6ee7729aa7041f1e1c2a8331"

# Patch to be applied
SRC_URI += " file://0001-TFLite-allow-BUILD_WITH_NNAPI-Makefile-variable-to-d.patch "
SRC_URI += " file://0002-TFLite-pip-package-use-local-BUILD_ROOT-directory.patch "
SRC_URI += " file://0003-TFLite-pip-package-support-cross-compiling-environme.patch "
SRC_URI += " file://0004-TFLite-pip-package-fix-python-execution-issue.patch "
SRC_URI += " file://0005-TFLite-pip-package-fix-_interpreter_wrapper.so-undef.patch "

S = "${WORKDIR}/tensorflow-${PV}-${PR}"

inherit setuptools3


PACKAGES += "${PN}-examples ${PN}-python3"

DEPENDS = " \
	curl-native \
	gzip-native \
	unzip-native \
	swig-native \
	python3-wheel-native \
	python3-numpy-native \
	python3 \
	zlib \
"

RDEPENDS_${PN}-python3 += " \
	python3-numpy \
	python3-pillow \
"

#Recipe only tested with armv7ve
COMPATIBLE_HOST = "(arm.*).*-linux"
COMPATIBLE_HOST_armv4  = 'null'
COMPATIBLE_HOST_armv5  = 'null'
COMPATIBLE_HOST_armv6  = 'null'
COMPATIBLE_HOST_armv7a = 'null'
COMPATIBLE_HOST_aarch64_class-target = 'null'

do_configure(){
	export HTTP_PROXY=${http_proxy}
	export HTTPS_PROXY=${https_proxy}
	export http_proxy=${http_proxy}
	export https_proxy=${https_proxy}

	# fix CURL certificates path
	export CURL_CA_BUNDLE="/etc/ssl/certs/ca-certificates.crt"

	${S}/tensorflow/lite/tools/make/download_dependencies.sh
}

# Set building environment variables
TENSORFLOW_TARGET="${@bb.utils.contains('TARGET_OS', 'linux-gnueabi', 'linux', '', d)}"
TENSORFLOW_TARGET_ARCH_armv7ve="${@bb.utils.contains('TUNE_FEATURES', 'armv7ve', 'armv7l', '', d)}"

do_compile () {
	export TENSORFLOW_TARGET=${TENSORFLOW_TARGET}
	export TENSORFLOW_TARGET_ARCH=${TENSORFLOW_TARGET_ARCH}
	export TENSORFLOW_CC_PREFIX=${CCACHE}${HOST_PREFIX}
	export TENSORFLOW_EXTRA_CXXFLAGS="${TARGET_CC_ARCH} ${TOOLCHAIN_OPTIONS}"
	export TENSORFLOW_BUILD_WITH_NNAPI=false

	sh ${S}/tensorflow/lite/tools/pip_package/build_pip_package.sh
}

do_install(){
	# tensorflow-lite static lib for dev and examples
	install -d ${D}${libdir}
	install -d ${D}${includedir}/tensorflow_lite
	install -d ${D}${bindir}/${PN}-${PV}/examples

	install -m install -m 0644 ${S}/tensorflow/lite/tools/make/gen/${TENSORFLOW_TARGET}_${TENSORFLOW_TARGET_ARCH}/lib/* ${D}${libdir}

	cd ${S}/tensorflow/lite
	cp --parents $(find . -name "*.h*") ${D}${includedir}/tensorflow_lite

	install -m 0555 ${S}/tensorflow/lite/tools/make/gen/${TENSORFLOW_TARGET}_${TENSORFLOW_TARGET_ARCH}/bin/minimal         ${D}${bindir}/${PN}-${PV}/examples
	install -m 0555 ${S}/tensorflow/lite/tools/make/gen/${TENSORFLOW_TARGET}_${TENSORFLOW_TARGET_ARCH}/bin/benchmark_model ${D}${bindir}/${PN}-${PV}/examples

	# tensorflow-lite python3 interpreter
	install -d ${D}${PYTHON_SITEPACKAGES_DIR}/tflite_runtime
	install -d ${D}${PYTHON_SITEPACKAGES_DIR}/tflite_runtime/lite
	install -d ${D}${PYTHON_SITEPACKAGES_DIR}/tflite_runtime/lite/python

	install -m 0644 ${S}/tensorflow/lite/tools/pip_package/gen/tflite_pip/build/lib.*/tflite_runtime/*.py             ${D}${PYTHON_SITEPACKAGES_DIR}/tflite_runtime
	install -m 0644 ${S}/tensorflow/lite/tools/pip_package/gen/tflite_pip/build/lib.*/tflite_runtime/lite/*.py        ${D}${PYTHON_SITEPACKAGES_DIR}/tflite_runtime/lite
	install -m 0644 ${S}/tensorflow/lite/tools/pip_package/gen/tflite_pip/build/lib.*/tflite_runtime/lite/python/*.py ${D}${PYTHON_SITEPACKAGES_DIR}/tflite_runtime/lite/python
	install -m 0755 ${S}/tensorflow/lite/tools/pip_package/gen/tflite_pip/build/lib.*/tflite_runtime/lite/python/*.so ${D}${PYTHON_SITEPACKAGES_DIR}/tflite_runtime/lite/python
}

ALLOW_EMPTY_${PN} = "1"

FILES_${PN} = ""

FILES_${PN}-staticdev = " \
	${includedir} \
	${libdir}/*.a \
"

FILES_${PN}-examples = " \
	${bindir}/${PN} \
	${bindir}/${PN}-${PV}/examples/minimal \
	${bindir}/${PN}-${PV}/examples/benchmark_model \
"

FILES_${PN}-python3 = " \
	${PYTHON_SITEPACKAGES_DIR}/tflite_runtime \
"