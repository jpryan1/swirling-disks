package simulation;

import java.util.ArrayList;



public class Spheres {
	static Sphere[] Spheres;
	static double[] boundpos;
	static double[] boundvel;
	static double swirlTime;
	static int index;
	static double swirl_interval = Constants.swirl_interval;
	public static void init(){
		Spheres = new Sphere[Constants.NUM_OF_SPHERES];
		index = 0;
		boundpos = new double[2];
		boundvel = new double[2];
		boundpos[0]=0;
		boundpos[1]=0;
		boundvel[0]=Constants.bound_vel;
		boundvel[1]=0;
		swirlTime=0;
	}
	public static void add(Sphere a){
		Spheres[index++] = a;
	}

	public static Collision nextCollision(Sphere a, Sphere b){ //THESE SUMS CAN BE GENERALIZED
		//		Vector vel1 = new Vector(a.vel);
		//		Vector vel2 = new Vector(b.vel);
		//		Vector pos1 = new Vector(a.pos);
		//		Vector pos2 = new Vector(b.pos);
		//		Vector dif = pos1.minus(pos2);
		//		if(vel2.dot(dif)<=0&&vel1.dot(dif)>0){
		//			return new Collision(a,b,-1);
		//		}



		double dv0 = a.vel[0]-b.vel[0];
		double dv1 = a.vel[1]-b.vel[1];
		double dp0 = a.pos[0]-b.pos[0];
		double dp1 = a.pos[1]-b.pos[1];

		double A = Math.pow(dv0,2)+Math.pow(dv1,2);
		double B = 2*( dp0* dv0 + dp1 *dv1);
		double C = Math.pow(dp0,2)+Math.pow(dp1,2)-4;//ball radius
		double disc = B*B-4*A*C;
		if(disc<0){
			return new Collision(a,b,-1);
		}
		//now we know it's real
		double toRet = (0.0-B-Math.sqrt(disc))/(2*A);
		if(toRet>1e-13){
			return new Collision(a,b,toRet);
		}
		return new Collision(a,b,-1);
		//		
		//		toRet = (0.0-B+Math.sqrt(disc))/(2*A);
		//		if(toRet>1e-13){
		//			return new Collision(a,b,toRet);
		//		}return new Collision(a,b,-1);

	}

	public static Collision nextWallCollision(Sphere a){//for now let's just deal with circle
		//We check if velocity dot product with position is positive, else no collision
		//This probably doesn't work for the boundary in motion, so we'll deal with that later

		double dv0 = a.vel[0]-boundvel[0];
		double dv1 = a.vel[1]-boundvel[1];
		double dp0 = a.pos[0]-boundpos[0];
		double dp1 = a.pos[1]-boundpos[1];

		double A = Math.pow(dv0,2)+Math.pow(dv1,2);
		double B = 2*( dp0* dv0 + dp1 *dv1);
		double C = Math.pow(dp0,2)+Math.pow(dp1,2)-Math.pow(Constants.BOUNDRAD-1, 2);
		double disc = B*B-4*A*C;
		if(disc<0){
			return new Collision(a,-1);
		}
		//now we know it's real
		//		double toRet = (0.0-B-Math.sqrt(disc))/(2*A);
		//		
		//		
		//		if(toRet>1e-13){
		//			return new Collision(a,toRet);
		//		}
		double toRet = (0.0-B+Math.sqrt(disc))/(2*A);
		if(toRet>1e-13){
			return new Collision(a,toRet);
		}

		return new Collision(a,-1);

	}




	public static void updatePositions(double time){
		Sphere sphere;
		for(int i=0;i<Spheres.length;i++){
			sphere = Spheres[i];
			sphere.pos[0] = sphere.pos[0]+time*sphere.vel[0];
			sphere.pos[1] = sphere.pos[1]+time*sphere.vel[1];
		}
		boundpos[0]+=time*boundvel[0];
		boundpos[1]+=time*boundvel[1];

	}

	public static ArrayList<Collision> nextCollision(){ //returns the two spheres 
		ArrayList<Collision> toRet = new ArrayList<Collision>();
		Sphere a,b;
		Collision col;
		for(int i=0;i<Spheres.length;i++){

			a = Spheres[i];
			for(int j=i+1;j<Spheres.length; j++){
				b = Spheres[j];

				col = nextCollision(a,b);

				if(col.time!=-1){
					if(toRet.size()==0){
						toRet.add(col);
					}else if(toRet.get(0).time-col.time>=1e-13){
						//*SEE NOTE IN NEXT ELSEIF BLOCK

						for(int k=0;k<toRet.size();){ //THE REASON FOR THIS FOR LOOP IS THAT WE USE get(0) AS THE BASE
							Collision c = toRet.get(k);
							if(Math.abs(c.time-col.time)>1e-13){
								//THIS FOR LOOP HAS CLEARED toRet FOR ALL CASES OTHER THAN SAME TIME COLLISIONS
								toRet.remove(k);
							}else{
								//System.out.println("HELLO");
								k++;
							}
						}
						toRet.add(col);
					}
					else if(Math.abs(toRet.get(0).time-col.time)<=1e-13){  
						//*IMPORTANT NOTE - THIS HAS ONLY BEEN FOUND TO HAPPEN FOR EXACT SAME TIME COLLISIONS
						//System.out.println("SAME TIME");
						toRet.add(col);
					}
				}
			}		
			col = nextWallCollision(a);
			if(col.time!=-1){
				if(toRet.size()==0){
					toRet.add(col);
				}else if(toRet.get(0).time-col.time>=1e-13){
					//See *comments for spheres for loop above
					for(int j=0;j<toRet.size();){
						Collision c = toRet.get(j);
						if(Math.abs(c.time-col.time)>1e-13){
							toRet.remove(j);
						}else{
							j++;
						}
					}

					toRet.add(col);
				}
				else if(Math.abs(toRet.get(0).time-col.time)<=1e-13){ 
					toRet.add(col);
				}
			}

		}if(Constants.bound_vel!=0){
			double swirlHit = (swirl_interval-swirlTime)/Constants.bound_vel;
			if(toRet.get(0).time-swirlHit>=1e-13){
				for(int j=0;j<toRet.size();){
					Collision c = toRet.get(j);
					if(Math.abs(c.time-swirlHit)>1e-13){
						toRet.remove(j);
					}else{

						j++;
					}
				}
				toRet.add(new Collision(swirlHit));
				swirlTime=0;
			}
			else if(Math.abs(toRet.get(0).time-swirlHit)<=1e-13){ 
				//SEE * comments above
				toRet.add(new Collision(swirlHit));
				swirlTime=0;
			}

			else{
				swirlTime+=Constants.bound_vel*toRet.get(0).time;
			}
		}
		return toRet;
	}
	public static double norm(double[] vel){
		double toRet=0;
		for(double i : vel){
			toRet+=Math.pow(i, 2);
		}
		return Math.sqrt(toRet);
	}
	public static double getEnergy(){
		double toRet = 0;
		for(Sphere s : Spheres){
			toRet += Math.pow(s.vel[0], 2)+Math.pow(s.vel[1], 2);	
		}return toRet;
	}
	public static double getMomenta(){
		double[] center = {0,0};
		for(Sphere s:Spheres){
			center[0] += s.pos[0];
			center[1] += s.pos[1];
		}
		center[0]/=Constants.NUM_OF_SPHERES;
		center[1]/=Constants.NUM_OF_SPHERES;
		
		double total = 0;
		double[] radius = new double[2];
		Vector rad,vel;
		for(Sphere s: Spheres){
			radius[0] = s.pos[0]-center[0];
			radius[1] = s.pos[1]-center[1];
			rad = new Vector(radius);
			if(rad.norm()!=0){ //this is so unlikely
				rad = rad.times(1.0/rad.norm());
			}
			radius = rad.toArray();
			double temp = - radius[1];
			radius[1] = radius[0];
			radius[0] = temp;
			rad = new Vector(radius);
			
			vel = new Vector(s.vel);
			total += vel.dot(rad);
			
			
		}
		return total;
	}
	public static void updateVelocities(){
		if(Constants.MU==0||Constants.newProcess){
			return; //duh
		}
		double norm,temp;
		for(Sphere s: Spheres){
			norm =norm(s.vel);
			if(norm==0) continue;
			for(int i=0;i<Constants.DIMENSIONS;i++){//for loop saves some lines of code
				temp = s.vel[i];
				s.vel[i]=s.vel[i]-Constants.MU*(s.vel[i]/norm);
				if(s.vel[i]*temp<=0){
					s.vel[i]=temp;
					if(temp>0){//it was originally positive
						s.vel[i]=0.01;
					}else{
						s.vel[i]=-0.01;
					}
				}
			}
		}
	}

}



//public static boolean directionCheck(Sphere a,Sphere b){//returns true iff they're heading towards each other
//Vector avel = new Vector(a.vel);
//Vector apos = new Vector(a.pos);
//Vector bvel = new Vector(b.vel);
//Vector bpos = new Vector(b.pos);
//if(a.id==2&&b.id==5) System.out.println(avel.dot(bpos.minus(apos))+" "+bvel.dot(apos.minus(bpos)));
//if(avel.dot(bpos.minus(apos))<=0&&bvel.dot(apos.minus(bpos))<=0){
//	if(a.id==2&&b.id==5)System.out.println("Fail");
//	return false;
//}
//return true;
//}