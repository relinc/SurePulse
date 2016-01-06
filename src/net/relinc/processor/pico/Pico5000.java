package net.relinc.processor.pico;

public class Pico5000 {
	
	   private PicoScope picoScope;
	   short pico5000Handle;
	   
	   public Pico5000()
	   {
	      picoScope = new PicoScope();
	      pico5000Handle = picoScope.ps5000OpenUnit();
	      
	      System.out.println("Handle: " + pico5000Handle);
	      
	      short close_status = getPicoScope().ps5000CloseUnit(getPico5000Handle());
		   
	      System.out.println("Exit status: " + close_status); 
	      System.exit(0); 
	   }

	   public short getPico5000Handle()
	   {
	      return pico5000Handle;
	   }
	   
	   public PicoScope getPicoScope() 
	   {
	        return picoScope;
	   }
}
