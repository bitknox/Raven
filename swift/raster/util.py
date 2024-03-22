def generate_tfw(name,height, width):
	with open(name+'.txt','w') as the_file:
		pixelScaleX = 360 / width
		pixelScaleY = -180 / height
		the_file.write('		                   '+ str(pixelScaleX)+'\n')
		the_file.write('		                   0.00000000000000\n')
		the_file.write('		                   0.00000000000000\n')
		the_file.write('		                  '+str(pixelScaleY)+'\n')
		the_file.write('		                 -180.00000000000000\n')
		the_file.write('		                   90.00000000000000\n')