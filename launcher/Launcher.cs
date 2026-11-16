using System;
using System.IO;
using System.Diagnostics;
using System.Windows.Forms;
class Program {
    [STAThread] static int Main(string[] args) {
        string root=AppDomain.CurrentDomain.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
        bool check=args.Length>0&&args[0]=="--check";
        bool settings=args.Length>0&&args[0]=="--settings";
        try {
            string java=Path.Combine(root,"runtime","jre","bin",check?"java.exe":"javaw.exe");
            string cp=Path.Combine(root,"patches","classes");
            var info=new ProcessStartInfo(java,"-Dfile.encoding=UTF-8 -cp \""+cp+"\" solo.Launcher \""+root+"\""+(check?" --check":settings?" --settings":""));
            info.WorkingDirectory=root;info.UseShellExecute=false;info.CreateNoWindow=true;
            using(var p=Process.Start(info)){p.WaitForExit();return p.ExitCode;}
        }catch(Exception e){if(!check)MessageBox.Show(e.Message,"MyRuneS MMO",MessageBoxButtons.OK,MessageBoxIcon.Error);return 1;}
    }
}
