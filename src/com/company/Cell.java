package com.company;

import com.company.Utilities.LinkedList;

public class Cell
{
    public LinkedList<Particle> particles;

    public Cell() {
        particles = new LinkedList<Particle>();
    }

    public void addParticle(Particle particle) {
        particles.insertElement(particle);
    }

    public void removeParticle(Particle particle) {
        particles.deleteElement(particle);
    }
}
