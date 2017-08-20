library(dplyr)
X_train=read.table(
  "train/X_train.txt", header=FALSE)
Y_train=read.table(
  "train/Y_train.txt", header=FALSE)
subject_train=read.table(
  "train/subject_train.txt", header=FALSE)
colNames=read.table("features.txt")
colnames(X_train)=colNames[[2]]
colnames(Y_train)="Activity"
colnames(subject_train)="Subject"
X_train$id=1:nrow(X_train)
Y_train$id=1:nrow(Y_train)
subject_train$id=1:nrow(subject_train)
train_complete=merge(X_train,Y_train,by.x="id",by.y="id")
train_complete_all=merge(train_complete,subject_train,by.x="id",by.y="id")

#Get test dataset 

X_test=read.table(
  "test/X_test.txt", header=FALSE)
Y_test=read.table(
  "test/Y_test.txt", header=FALSE)
subject_test=read.table(
  "test/subject_test.txt", header=FALSE)
colnames(X_test)=colNames[[2]]
colnames(Y_test)="Activity"
colnames(subject_test)="Subject"
X_test$id=1:nrow(X_test)
Y_test$id=1:nrow(Y_test)
subject_test$id=1:nrow(subject_test)
test_complete=merge(X_test,Y_test,by.x="id",by.y="id")
test_complete_all=merge(test_complete,subject_test,by.x="id",by.y="id")
dataset_complete=rbind(train_complete_all,test_complete_all)
dataset_meanstd=dataset_complete[,grep("mean|std", colnames(dataset_complete))]

#Set activity name
activity_labels=read.table("activity_labels.txt")


