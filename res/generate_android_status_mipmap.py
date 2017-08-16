#!/usr/bin/env python
# -*- coding: utf-8 -*-

# A python script for GIMP image editor https://www.gimp.org/
# Generates 18x18, 24x24 etc. icons in folders mipmap-ldpi, mipmap-mdpi etc.
# Put it into GIMP plug-ins folder and select [Batch] > [Generate android status bar icon mipmap]

from gimpfu import *
import os

def generate_android_status_mipmap(image, drawable):
  
  dimentions = {'l': 18, 'm':24, 'h':36, 'xh':48}
  directory = os.path.dirname(image.filename)

  basename = 'ic_stat_notify'

  for key, size in dimentions.items():
    scaled = pdb.gimp_image_duplicate(image)
    pdb.gimp_context_set_interpolation(INTERPOLATION_LANCZOS)
    pdb.gimp_image_scale(scaled, size, size)
    layer = pdb.gimp_image_merge_visible_layers(scaled, EXPAND_AS_NECESSARY)

    subdirname = 'mipmap-' + key + 'dpi'
    subdirpath = os.path.join(directory, subdirname)
    try:
      os.mkdir(subdirpath)
    except OSError:
      pass

    scaled_name = os.path.join(subdirpath, basename + '.png')
    pdb.file_png_save_defaults(scaled, layer, scaled_name, scaled_name)
    pdb.gimp_image_delete(scaled)


register(
          "python-fu-generate-android-status-mipmap", # function name
          "Generates android status bar icon mipmap", # info
          "Generates 18x18, 24x24 etc. icons in folders mipmap-ldpi, mipmap-mdpi etc.", # short description
          "sonnayasomnambula", # author
          "sonnayasomnambula", # copywrite
          "15.08.2017", # date
          "Generate android status bar icon mipmap", # menu item
          "*", # image types
          [
              (PF_IMAGE, "image", "Input image", None), # image
              (PF_DRAWABLE, "drawable", "Input drawable", None) # layer
              
          ],
          [], # return values
          generate_android_status_mipmap, menu="<Image>/Batch/") # function name and menu item position

main()