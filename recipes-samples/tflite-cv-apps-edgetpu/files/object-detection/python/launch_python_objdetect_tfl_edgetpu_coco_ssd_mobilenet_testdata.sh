#!/bin/sh
python3 /usr/local/demo-ai/computer-vision/tflite-object-detection-edgetpu/python/objdetect_tfl_edgetpu.py -m /usr/local/demo-ai/computer-vision/models/coco_ssd_mobilenet/detect_edgetpu.tflite -l /usr/local/demo-ai/computer-vision/models/coco_ssd_mobilenet/labels.txt -i /usr/local/demo-ai/computer-vision/models/coco_ssd_mobilenet/testdata/
