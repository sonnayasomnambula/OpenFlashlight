#!/usr/bin/env python
# -*- coding: utf-8 -*-

# A python script for GIMP image editor https://www.gimp.org/
# Generates 36x36, 48x48, 72x72 etc. icons in folders mipmap-mdpi, mipmap-hdpi etc.
# Put it into GIMP plug-ins folder and select [Batch] > [Generate android launcher icon mipmap]

from gimpfu import *
import os

def generate_android_launcher_mipmap(image, drawable):
  
  dimentions = {'l': 36, 'm':48, 'h':72, 'xh':96, 'xxh':144, 'xxxh':192}
  directory = os.path.dirname(image.filename)

  basename = 'ic_launcher'

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
          "python-fu-generate-android-launcher-mipmap", # function name
          "Generates android launcher icon mipmap", # info
          "Generates 36x36, 48x48, 72x72 etc. icons in folders mipmap-ldpi, mipmap-mdpi, mipmap-hdpi etc.", # short description
          "sonnayasomnambula", # author
          "sonnayasomnambula", # copywrite
          "14.08.2017", # date
          "Generate android launcher icon mipmap", # menu item
          "*", # image types
          [
              (PF_IMAGE, "image", "Input image", None), # image
              (PF_DRAWABLE, "drawable", "Input drawable", None) # layer
              
          ],
          [], # return values
          generate_android_launcher_mipmap, menu="<Image>/Batch/") # function name and menu item position

main()