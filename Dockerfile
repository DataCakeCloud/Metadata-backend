
# stage2 build java runtime image
# FROM 848318613114.dkr.ecr.us-east-1.amazonaws.com/shareit-common/java-base:oracle-jdk-1.8.0_202
FROM swr.ap-southeast-3.myhuaweicloud.com/shareit-common/java-base:oracle-jdk-1.8.0_202


# copy run jar file
COPY target/*.jar /data/code/
COPY run.sh  /data/code/run.sh
RUN sudo chmod o+x /data/code/run.sh    && \
    sudo chown -R ${AppUser}:${AppGroup} /data

# set workdir
WORKDIR /data/code
CMD ["/bin/bash","-c","bash /data/code/run.sh start $Env"]
