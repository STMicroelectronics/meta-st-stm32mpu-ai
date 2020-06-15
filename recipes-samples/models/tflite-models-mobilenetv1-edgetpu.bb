# Copyright (C) 2019, STMicroelectronics - All Rights Reserved
SUMMARY = "Create package containing MobileNetV1 models for EdgeTPU "
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI  = " https://raw.githubusercontent.com/google-coral/edgetpu/master/test_data/mobilenet_v1_1.0_224_quant_edgetpu.tflite;subdir=${BPN}-${PV}/mobilenet_v1_1.0_224_quant_edgetpu;name=mobilenet_v1_1.0_224_quant_edgetpu.tflite"
SRC_URI[mobilenet_v1_1.0_224_quant_edgetpu.tflite.md5sum] = "06b5764f4a5063903cac48c9fc96d8e5"
SRC_URI[mobilenet_v1_1.0_224_quant_edgetpu.tflite.sha256sum] = "e17124a57e78f51af6600a561a76cc13866ab9d039378b3dc83507941957f4b0"

S = "${WORKDIR}/${BPN}-${PV}"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${prefix}/local/demo-ai/computer-vision/models/mobilenet

    # install mobilenet models
    install -m 0644 ${S}/mobilenet_v1_1.0_224_quant_edgetpu/*.tflite ${D}${prefix}/local/demo-ai/computer-vision/models/mobilenet/
}

PACKAGES_remove = "${PN}-dbg"
PACKAGES_remove = "${PN}-dev"
PACKAGES_remove = "${PN}-staticdev"

FILES_${PN} += "${prefix}/local/"

RDEPENDS_${PN} += " tflite-models-mobilenetv1 "
