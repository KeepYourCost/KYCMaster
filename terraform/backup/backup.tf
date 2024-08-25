provider "aws" {
  profile    = "default"
  access_key = var.access_key
  secret_key = var.secret_key
  region     = "ap-northeast-2"
}

resource "aws_security_group" "sg_kyc" {
  name = "Kyc Security Group"
  tags = { Name = "Kyc Security Group" }
}

resource "aws_security_group_rule" "sgr-in-ssh" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.sg_kyc.id
}

resource "aws_security_group_rule" "sgr-out-http" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.sg_kyc.id
}

variable "spot_instance_type" {
  type    = string
  default = "t2.small"
}

variable "instance_count" {
  type    = number
  default = 1
}

resource "aws_key_pair" "key_pair" {
  key_name   = "kyc"
  public_key = file("~/.ssh/kyc.pub")
}

resource "aws_spot_instance_request" "kyc_spot" {
  count                          = var.instance_count
  ami                            = "ami-04cebc8d6c4f297a3"
  spot_price                     = "0.016"
  instance_type                  = var.spot_instance_type
  instance_interruption_behavior = "terminate"
  key_name                       = aws_key_pair.key_pair.key_name
  vpc_security_group_ids         = [aws_security_group.sg_kyc.id]
  wait_for_fulfillment           = true

  tags = {
    Name = "KycSpot-${count.index + 1}"
  }
  
}

resource "aws_instance" "kyc_spot_instance" {
  count           = var.instance_count
  ami             = aws_spot_instance_request.kyc_spot[count.index].ami
  instance_type   = aws_spot_instance_request.kyc_spot[count.index].instance_type
  key_name        = aws_spot_instance_request.kyc_spot[count.index].key_name
  vpc_security_group_ids   = aws_spot_instance_request.kyc_spot[count.index].vpc_security_group_ids
  user_data = <<-EOF
    #!/bin/bash
    cat <<EOT > /etc/manifest.txt
    backupDir=/etc/all
    EOT
  EOF


  tags = {
    Name = "KycSpotInstance-${count.index + 1}"
  }
}

output "public_ips" {
  value       = aws_instance.kyc_spot_instance[*].public_ip
  description = "Public IPs of the Spot instances"
}

output "public_dns" {
  value       = aws_instance.kyc_spot_instance[*].public_dns
  description = "Public DNS of the Spot instances"
}

output "instance_ids" {
  value       = aws_instance.kyc_spot_instance[*].id
  description = "IDs of the Spot instances"
}
