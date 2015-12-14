if [ -d $HOME/Eclipse/jdk1.8.0_60 ]
then
    export JAVA_HOME=$HOME/Eclipse/jdk1.8.0_60
else
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home
fi
if [ -d $HOME/Eclipse/apache-maven-3.2.3 ]
then
    export M2_HOME=$HOME/Eclipse/apache-maven-3.2.3
else
    export M2_HOME=$HOME/Eclipse/apache-maven-3.3.3
fi

export PATH="$M2_HOME/bin:$JAVA_HOME/bin:$PATH"

export CSS_REPO=file:$HOME/git/org.csstudio.sns/repository/target/repository

