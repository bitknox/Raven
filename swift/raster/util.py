def generate_tfw(name, height, width, area):
	north,west,south,east = area
	with open(name+'.tfw','w') as the_file:
		pixelScaleX = (east-west) / width
		pixelScaleY = (south-north) / height
		the_file.write('		                   '+ str(pixelScaleX)+'\n')
		the_file.write('		                   0.00000000000000\n')
		the_file.write('		                   0.00000000000000\n')
		the_file.write('		                  '+str(pixelScaleY)+'\n')
		the_file.write('		                 '+str(west)+'\n')
		the_file.write('		                   '+str(north)+'\n')