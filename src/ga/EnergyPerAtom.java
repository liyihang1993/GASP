/*
 * Copyright 2011-2014 Will Tipton, Richard Hennig, Ben Revard, Stewart Wenner

This file is part of the Genetic Algorithm for Structure and Phase Prediction (GASP).

    GASP is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GASP is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GASP.  If not, see <http://www.gnu.org/licenses/>.
    
    
    */

package ga;

import java.util.List;

import mopac.MopacEnergy;
import avogadro.AvogadroEnergy;
import castep.CastepEnergy;
import dlpoly.DLPolyEnergy;
import gulp.GulpEnergy;
import utility.Utility;
import vasp.VaspEnergy;

// EnergyPerAtom is an ObjectiveFunction.  It uses an Energy object to compute the
// energy of a StructureOrg and then normalizes that by the number of atoms in the
// StructureOrg to get the Organisms's value.

public class EnergyPerAtom extends ObjectiveFunction {
	
	Energy energyFcn;
	StructureOrg org;

	public EnergyPerAtom (List<String> args, Organism o) {
		if (args == null || args.size() < 1)
			GAParameters.usage("Not enough parameters given to EnergyPerAtom", true);
		
		String energyType = args.get(0);
		if (energyType.equalsIgnoreCase("gulp"))
			energyFcn = new GulpEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("vasp"))
			energyFcn = new VaspEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("ohmms"))
			energyFcn = new OhmmsEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("lammps"))
			energyFcn = new LammpsEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("castep"))
			energyFcn = new CastepEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("avogadro"))
			energyFcn = new AvogadroEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("dlpoly"))
			energyFcn = new DLPolyEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("mopac"))
			energyFcn = new MopacEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("dftpp"))
			energyFcn = new DFTPPEnergy(Utility.subList(args, 1));
		else if (energyType.equalsIgnoreCase("generic"))
			energyFcn = new GenericEnergy(Utility.subList(args, 1));
		else
			throw new RuntimeException("Unknown energy function in EnergyPerAtom: " + energyType);
		
		org = (StructureOrg)o;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		result.append("EnergyPerAtom objective function with energy function: " + GAUtils.newline());
		result.append(energyFcn);
		
		return result.toString();
	}
	
	public Thread evaluate() {		
		// short circuit here if we've done the calculation already
		if (org.knowsValue())
			return null;
		
		// 
		if (energyFcn.cannotCompute(org)) {
			GAOut.out().stdout("Energy function could not compute " + org.getID() + ".", GAOut.NOTICE, org.getID());
			org.setTotalEnergy(Double.POSITIVE_INFINITY);
			org.setValue(Double.POSITIVE_INFINITY);
			return null;
		}
		
		// another total energy calculation:
		numCalculations++;
		
		// start the calculation and return the Thread
		Thread t = new Thread(this);
		t.start();
		return t;
	}
	
	public void run() {
		
		double totalEnergy = energyFcn.getEnergy(org);

		double value = totalEnergy / org.getCell().getNumSites();
		
		org.setTotalEnergy(totalEnergy);
		
		GAOut.out().stdout("Setting value of org " + org.getID() + " to " + value, GAOut.NOTICE, org.getID());
		
		org.setValue(value);
		
	}
}
