## Common Issues
- Thread stays NEW: did you call start()?
- Output seems ordered: run multiple times.
- Priority looks consistent: try under CPU load, or more runs.
- jstack not found: use jcmd <pid> Thread.print
