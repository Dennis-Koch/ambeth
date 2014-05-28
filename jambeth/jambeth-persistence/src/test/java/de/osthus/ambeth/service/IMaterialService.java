package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.model.Material;

public interface IMaterialService
{
	Material getMaterial(int id);

	List<Material> getAllMaterials();

	Material getMaterialByName(String name);

	void updateMaterial(Material material);

	void deleteMaterial(Material material);

	void updateMaterials(Material[] materials);
}