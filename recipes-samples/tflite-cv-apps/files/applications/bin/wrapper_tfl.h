/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

#ifndef WRAPPER_TFL_H_
#define WRAPPER_TFL_H_

#include <algorithm>
#include <functional>
#include <queue>
#include <memory>
#include <string>
#include <vector>

#include "tensorflow/lite/kernels/register.h"
#include "tensorflow/lite/model.h"
#include "tensorflow/lite/optional_debug_tools.h"
#include "tensorflow/lite/profiling/profiler.h"

namespace tflite {
namespace wrapper_tfl {

struct Config {
	bool verbose = false;
	bool accel = false;
	bool input_floating = false;
	bool profiling = false;
	bool allow_fp16 = false;
	int loop_count = 1;
	float input_mean = 127.5f;
	float input_std = 127.5f;
	int number_of_threads = 4;
	int number_of_results = 5;
	std::string model_name;
	std::string labels_file_name;
};

struct Interpreter {
	// Taking a reference to the (const) model data avoids lifetime-related issues
	// and complexity with the TFL_Model's existence.
	std::unique_ptr<tflite::FlatBufferModel> model;
	std::unique_ptr<tflite::Interpreter> impl;
	float inference_time;
};

struct Label_Results {
	float accuracy[10];
	int index[10];
	float inference_time;
};

struct ObjDetect_Location {
	float y0, x0, y1, x1;
};

struct ObjDetect_Results {
	float score[10];
	int index[10];
	struct ObjDetect_Location location[10];
	float inference_time;
};

int GetInputWidth(Interpreter* interpreter);
int GetInputHeight(Interpreter* interpreter);
int GetInputChannels(Interpreter* interpreter);
void DisplayModelInformation(Interpreter* interpreter);
Interpreter* InitInterpreter(Config* conf);
void RunInference(Config* conf, Interpreter* interpreter, uint8_t* img);
void GetLabelResults(Config* conf, Interpreter* interpreter, Label_Results* results);
void GetObjDetectResults(Config* conf, Interpreter* interpreter, ObjDetect_Results* results);
TfLiteStatus ReadLabelsFile(const std::string& file_name,
			    std::vector<std::string>* result,
			    size_t* found_label_count);

template <class T>
void get_top_n(T* prediction, int prediction_size, size_t num_results,
	       float threshold, std::vector<std::pair<float, int>>* top_results,
	       bool input_floating);

extern bool input_floating;

// Returns the top N confidence values over threshold in the provided vector,
// sorted by confidence in descending order.
template <class T>
void get_top_n(T* prediction, int prediction_size, size_t num_results,
	       float threshold, std::vector<std::pair<float, int>>* top_results,
	       bool input_floating) {
	// Will contain top N results in ascending order.
	std::priority_queue<std::pair<float, int>, std::vector<std::pair<float, int>>,
	std::greater<std::pair<float, int>>>
		top_result_pq;

	const long count = prediction_size;  // NOLINT(runtime/int)
	for (int i = 0; i < count; ++i) {
		float value;
		if (input_floating)
			value = prediction[i];
		else
			value = prediction[i] / 255.0;
		// Only add it if it beats the threshold and has a chance at being in
		// the top N.
		if (value < threshold) {
			continue;
		}

		top_result_pq.push(std::pair<float, int>(value, i));

		// If at capacity, kick the smallest value out.
		if (top_result_pq.size() > num_results) {
			top_result_pq.pop();
		}
	}

	// Copy to output vector and reverse into descending order.
	while (!top_result_pq.empty()) {
		top_results->push_back(top_result_pq.top());
		top_result_pq.pop();
	}
	std::reverse(top_results->begin(), top_results->end());
}

}  // namespace wrapper_tfl
}  // namespace tflite

#endif  // WRAPPER_TFL_H_
