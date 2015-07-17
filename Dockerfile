FROM jetty:jre7

# Atomhopper Version
ENV AH_VERSION 1.2.30

# Configuration & Data Directory
RUN mkdir -p /etc/atomhopper &&\
    mkdir -p /var/log/atomhopper &&\
	mkdir -p /opt/atomhopper
	
# Download Atomhopper Embedded Server
RUN curl -k -o /var/lib/jetty/webapps/ROOT.war https://maven.research.rackspacecloud.com/content/repositories/releases/org/atomhopper/atomhopper/${AH_VERSION}/atomhopper-${AH_VERSION}.war

# ADD etc /etc/atomhopper/

RUN chown -R jetty /opt/atomhopper &&\
    chown -R jetty /var/log/atomhopper
    
VOLUME ["/etc/atomhopper"]    