package com.ubi.sesdk.core.listeners;


/**
 * @author João Pedro Pedrosa, SE on 05/02/2016.
 */
public interface IDefinitionsAR {

    void OnPassFilterChange(float passfilter);

    void OnMaxDistanceChange(int maxDistance);

    void OnPullCloserDistanceChange(int pullCloserDistance);

    void OnPushAwayDistanceChange(int pushAwayDistance);

    void OnMaxDistanceToRenderChange(int maxDistanceToRender);

    void OnDistanceFactorChange(int distanceFactor);
}
