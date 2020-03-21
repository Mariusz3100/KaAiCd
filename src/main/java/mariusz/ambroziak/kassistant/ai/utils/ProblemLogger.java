package mariusz.ambroziak.kassistant.ai.utils;


public class ProblemLogger {
	public static final int singleMessageLength = 90;
	
	public static void logProblem(String message){
		System.err.println(message);
		
	}
	
	
	
	public static void logStackTrace( StackTraceElement[] stackTrace){
		if(stackTrace.length==0||stackTrace==null){
//			Problem p=new Problem(0l,true,"Empty StackTrace recorded",false);
//			DaoProvider.getInstance().getProblemDao().addProblem(p);
			logProblem("Empty StackTrace recorded");
		}else{
			String stackTraceMessage="";

			for(int i=0;i<stackTrace.length;i++){
				stackTraceMessage+="<p style=\"text-indent:"+i*10+"px\">"+stackTrace[i].toString();
			}
			logProblem(stackTraceMessage);
		}
	}
	
	
	public static void main(String[] args){
		ProblemLogger.logProblem("test problem....."
				+ "................................ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd................................................................................................................................................................");
		
	}
}
