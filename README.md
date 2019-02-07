# GraECfully
AWS provides a 2 minute warning to spot instances which are about to be shutdown via its instance metadata service `http://169.254.169.254/latest/meta-data/spot/termination-time`and on CloudWatch.  This mod gracefully stops forge servers before shutdown by polling the above URL every 20 seconds.

## Releases
https://github.com/themooer1/GraECfully/releases
