package dk.itu.raven.io;

import java.awt.Rectangle;

import dk.itu.raven.geometry.Offset;

public class RasterWindow {
	public Rectangle position;
	public RasterReader reader;
	public Offset<Integer> offset;

	public RasterWindow(RasterReader reader, Rectangle position, Offset<Integer> offset) {
		this.reader = reader;
		this.position = position;
		this.offset = offset;
	}
}
