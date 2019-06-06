package net.sf.hale.particle;

import de.matthiasmann.twl.Color;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONObject;

public abstract class AnimationBase implements Saveable
{

    private final int halfWidth;
    private final int halfHeight;

    private float r, g, b, a;
    private float vr, vg, vb, va;

    private float r2, g2, b2;
    private float vr2, vg2, vb2;

    private float positionX, positionY;
    private float velocityX, velocityY;
    private float speed;
    private float velocityAngle;

    private float rotation;
    private float rotationSpeed;

    private float secondsRemaining;

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        // use java.awt.color which is more robust than the TWL color and can
        // convert floats RGBA to a color integer
        data.put( "color", getColorString( new java.awt.Color( r, g, b, a ) ) );

        data.put( "colorVelocity", getColorString( new java.awt.Color( vr, vg, vb, va ) ) );

        data.put( "secondaryColor", getColorString( new java.awt.Color( r2, g2, b2 ) ) );

        data.put( "secondaryColorVelocity", getColorString( new java.awt.Color( vr2, vg2, vb2 ) ) );

        data.put( "positionX", positionX );
        data.put( "positionY", positionY );
        data.put( "velocityX", velocityX );
        data.put( "velocityY", velocityY );

        data.put( "rotation", rotation );
        data.put( "rotationSpeed", rotationSpeed );

        data.put( "secondsRemaining", secondsRemaining );

        return data;
    }

    protected final void loadBase( SimpleJSONObject data )
    {
        secondsRemaining = data.get( "secondsRemaining", 0.0f );
        rotationSpeed = data.get( "rotationSpeed", 0.0f );
        rotation = data.get( "rotation", 0.0f );

        velocityY = data.get( "velocityY", 0.0f );
        velocityX = data.get( "velocityX", 0.0f );
        positionX = data.get( "positionX", 0.0f );
        positionY = data.get( "positionY", 0.0f );

        String colorString = data.get( "color", null );
        Color color = Color.parserColor( colorString );
        r = color.getRedFloat( );
        g = color.getGreenFloat( );
        b = color.getBlueFloat( );
        a = color.getAlphaFloat( );

        String colorVString = data.get( "colorVelocity", null );
        Color colorV = Color.parserColor( colorVString );
        vr = colorV.getRedFloat( );
        vg = colorV.getGreenFloat( );
        vb = colorV.getBlueFloat( );
        va = colorV.getAlphaFloat( );

        String color2String = data.get( "secondaryColor", null );
        Color color2 = Color.parserColor( color2String );
        r2 = color2.getRedFloat( );
        g2 = color2.getGreenFloat( );
        b2 = color2.getBlueFloat( );

        String color2VString = data.get( "secondaryColorVelocity", null );
        Color color2V = Color.parserColor( color2VString );
        vr2 = color2V.getRedFloat( );
        vg2 = color2V.getGreenFloat( );
        vb2 = color2V.getBlueFloat( );
    }

    private String getColorString( java.awt.Color color )
    {
        return '#' + String.format( "%08x", color.getRGB( ) );
    }

    public AnimationBase( int halfWidth, int halfHeight )
    {
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.rotation = 0.0f;
    }

    public AnimationBase( AnimationBase other )
    {
        this.halfWidth = other.halfWidth;
        this.halfHeight = other.halfHeight;

        this.r = other.r; this.g = other.g; this.b = other.b; this.a = other.a;
        this.vr = other.vr; this.vg = other.vg; this.vb = other.vb; this.va = other.va;

        this.r2 = other.r2; this.g2 = other.g2; this.b2 = other.b2;
        this.vr2 = other.vr2; this.vg2 = other.vg2; this.vb2 = other.vb2;

        this.positionX = other.positionX;
        this.positionY = other.positionY;

        this.velocityX = other.velocityX;
        this.velocityY = other.velocityY;

        this.speed = other.speed;
        this.velocityAngle = other.velocityAngle;

        this.secondsRemaining = other.secondsRemaining;
    }

    public boolean elapseTime( float seconds )
    {
        rotation += ( rotationSpeed * seconds );

        positionX += ( velocityX * seconds );
        positionY += ( velocityY * seconds );

        r += vr * seconds;
        g += vg * seconds;
        b += vb * seconds;
        a += va * seconds;

        r2 += vr2 * seconds;
        g2 += vg2 * seconds;
        b2 += vb2 * seconds;

        secondsRemaining -= seconds;

        return secondsRemaining <= 0.0f;
    }

    public final void setRotation( float angle )
    {
        this.rotation = angle;
    }

    public final void setRotationSpeed( float angle )
    {
        this.rotationSpeed = angle;
    }

    public final void offsetPosition( float x, float y )
    {
        this.positionX += x;
        this.positionY += y;
    }

    public final void setPosition( Point screenPoint )
    {
        this.positionX = screenPoint.x;
        this.positionY = screenPoint.y;
    }

    public final void setPosition( float x, float y )
    {
        this.positionX = x;
        this.positionY = y;
    }

    public final void setSecondaryRed( float r )
    {
        this.r2 = r;
    }

    public final void setSecondaryGreen( float g )
    {
        this.g2 = g;
    }

    public final void setSecondaryBlue( float b )
    {
        this.b2 = b;
    }

    public final void setSecondaryRedSpeed( float vr )
    {
        this.vr2 = vr;
    }

    public final void setSecondaryGreenSpeed( float vg )
    {
        this.vg2 = vg;
    }

    public final void setSecondaryBlueSpeed( float vb )
    {
        this.vb2 = vb;
    }

    public final void setRed( float r )
    {
        this.r = r;
    }

    public final void setGreen( float g )
    {
        this.g = g;
    }

    public final void setBlue( float b )
    {
        this.b = b;
    }

    public final void setAlpha( float a )
    {
        this.a = a;
    }

    public final void setAlphaSpeed( float va )
    {
        this.va = va;
    }

    public final void setRedSpeed( float vr )
    {
        this.vr = vr;
    }

    public final void setGreenSpeed( float vg )
    {
        this.vg = vg;
    }

    public final void setBlueSpeed( float vb )
    {
        this.vb = vb;
    }

    public void setDuration( float lifetime )
    {
        this.secondsRemaining = lifetime;
    }

    public final void setVelocity( float[] vector )
    {
        this.velocityX = vector[ 0 ];
        this.velocityY = vector[ 1 ];
        this.speed = vector[ 2 ];
        this.velocityAngle = vector[ 3 ];
    }

    public final void setVelocity( float vx, float vy )
    {
        this.velocityX = vx;
        this.velocityY = vy;
        this.speed = ( float ) Math.sqrt( vx * vx + vy * vy );
        this.velocityAngle = ( float ) Math.atan( vy / vx );
    }

    public final void setVelocityMagnitudeAngle( float magnitude, float angle )
    {
        this.speed = magnitude;
        this.velocityAngle = angle;
        this.velocityX = ( float ) Math.cos( angle ) * magnitude;
        this.velocityY = ( float ) Math.sin( angle ) * magnitude;

    }

    public void finish( )
    {
        this.secondsRemaining = 0.0f;
    }

    public final int getHalfWidth( ) { return halfWidth; }

    public final int getHalfHeight( ) { return halfHeight; }

    public final float getR( ) { return r; }

    public final float getG( ) { return g; }

    public final float getB( ) { return b; }

    public final float getA( ) { return a; }

    public final float getR2( ) { return r2; }

    public final float getG2( ) { return g2; }

    public final float getB2( ) { return b2; }

    public final float getX( ) { return positionX; }

    public final float getY( ) { return positionY; }

    public final float getVX( ) { return velocityX; }

    public final float getVY( ) { return velocityY; }

    public final float getSpeed( ) { return speed; }

    public final float getVelocityAngle( ) { return velocityAngle; }

    public final float getSecondsRemaining( ) { return secondsRemaining; }

    public final float getRotation( ) { return rotation; }

    public final float getRotationSpeed( ) { return rotationSpeed; }
}
