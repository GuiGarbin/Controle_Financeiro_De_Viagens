package org.example.dto.request;

// DTO de entrada para adicionar um ponto turistico a uma viagem
// (POST /api/trips/{id}/turistic-points). O custo e informado na moeda da viagem.
public class AddTuristicPointRequest {
    private String name;
    private double cost;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}
