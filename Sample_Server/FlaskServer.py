from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import argparse
import sys
import time
import os
import random

import numpy as np
import tensorflow as tf

import flask
from flask import Flask, request, redirect, url_for
from werkzeug.utils import secure_filename

import logging
from logging import Formatter
    

local_data = "imgs/"
UPLOAD_FOLDER = "imgs/"
ALLOWED_EXTENSIONS = set(['txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'])


app = flask.Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


def read_tensor_from_image_file(file_name, input_height=299, input_width=299,input_mean=0, input_std=255):
    with tf.Graph().as_default():
        input_name = "file_reader"
        output_name = "normalized"
        file_reader = tf.read_file(file_name, input_name)
        if file_name.endswith(".png"):
                image_reader = tf.image.decode_png(file_reader, channels = 3, name='png_reader')
        elif file_name.endswith(".gif"):
                image_reader = tf.squeeze(tf.image.decode_gif(file_reader, name='gif_reader'))
        elif file_name.endswith(".bmp"):
                image_reader = tf.image.decode_bmp(file_reader, name='bmp_reader')
        else:
                image_reader = tf.image.decode_jpeg(file_reader, channels = 3, name='jpeg_reader')
        float_caster = tf.cast(image_reader, tf.float32)
        dims_expander = tf.expand_dims(float_caster, 0);
        resized = tf.image.resize_bilinear(dims_expander, [input_height, input_width])
        normalized = tf.divide(tf.subtract(resized, [input_mean]), [input_std])
        with tf.Session() as tensor_session:
            #sess = tf.Session()
            result = tensor_session.run(normalized)
        return result


def load_labels(label_file):
    label = []
    proto_as_ascii_lines = tf.gfile.GFile(label_file).readlines()
    for l in proto_as_ascii_lines:
        label.append(l.rstrip())
    return label


def loadModel(model_path, input_layer, output_layer):
    
        input_name = "import/" + input_layer
        output_name = "import/" + output_layer
        
        graph = tf.Graph()
        graph_def = tf.GraphDef()

        with open(model_path, "rb") as f:
            graph_def.ParseFromString(f.read())
        with graph.as_default():
            tf.import_graph_def(graph_def)
            
            sess = tf.Session(graph=graph)
            input_operation = graph.get_operation_by_name(input_name);
            output_operation = graph.get_operation_by_name(output_name);
        
        return (sess, input_operation, output_operation)

@app.route('/infer',methods=['POST'])
def inference_main():
    
    if request.method == 'POST':
        
        # Save file to disk
        file = request.files['file']
        filename = secure_filename(file.filename)
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        
        local_filename = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        
    # Preprocess image
    t = read_tensor_from_image_file(local_filename,input_height=model_size,input_width=model_size)
    
    # Run inference
    results = sess.run(self.output_operation,{self.input_operation.outputs[0]: input_t})
    
    # Parse results
    results = np.squeeze(results)
    top_k = results.argsort()[-5:][::-1]
    result_name = labels[top_k[0]]
    confidence = results[top_k[0]]
    
    os.remove(local_filename)
    
    return "%s %s" % (result_name, confidence)


if __name__ == "__main__":
    
    
    if not os.path.exists(UPLOAD_FOLDER):
        os.makedirs(UPLOAD_FOLDER)
    
    path_to_model = "models/inception_v3.pb"
    model_input_layer = "input"
    model_output_layer = "InceptionV3/Predictions/Reshape_1"
    model_size = 299
    
    sess, input_operation, output_operation = loadModel(path_to_model, model_input_layer, model_output_layer)
    
    
    logging.basicConfig(level=logging.INFO)
    app.run(host="0.0.0.0", port=int("54321"), debug=True, use_reloader=False)
    
    
########################################
