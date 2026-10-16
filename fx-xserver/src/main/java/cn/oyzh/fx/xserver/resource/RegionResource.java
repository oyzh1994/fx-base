package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.resource.XResource;

import java.awt.Rectangle;
import java.util.List;

/**
 * A region: a list of rectangles a client can name, hand to another request, and read back.
 *
 * <p>One resource class serves the two extensions that traffic in them - XFixes, which creates and
 * edits them and reports a cursor's shape through one, and Composite, whose
 * {@code CreateRegionFromBorderClip} is how a compositor asks what part of a window is on screen.
 * That they share the class is the point: the protocol lets a region made by one be used by the
 * other, which is exactly what a compositor does with the border clip it just asked for.
 *
 * <p>The class id comes from one {@code newExtensionClass()} call rather than one per extension.
 * Two would type the same concept twice, and a region each extension could not see the other's.
 */
public final class RegionResource extends XResource {

    /** The region's resource class, shared by every extension that creates one. */
    public static final int CLASS = XResource.newExtensionClass();

    private List<Rectangle> rectangles;

    public RegionResource(int id, ClientConnection owner, List<Rectangle> rectangles) {
        super(id, CLASS, owner);
        this.rectangles = List.copyOf(rectangles);
    }

    public List<Rectangle> rectangles() {
        return rectangles;
    }

    public void setRectangles(List<Rectangle> rectangles) {
        this.rectangles = List.copyOf(rectangles);
    }

    @Override
    public String typeName() {
        return "region";
    }
}
